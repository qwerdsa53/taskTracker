# Graceful shutdown & rolling deploys

This project is configured for dev/prod parity, fast container startup, graceful
SIGTERM handling, and zero-downtime rolling updates in Docker Swarm.

## Что сделано

### Шаг 1. Унификация окружений
- `docker-compose.yml` и `docker-stack.yml` используют **одинаковые** pinned-теги
  образов: `postgres:16.4-alpine`, `redis:7.4-alpine`, `nginx:1.27-alpine`,
  `eclipse-temurin:21-jre-alpine`. Никаких «на маке 9.6, а на сервере 14».
- Все зависимости запускаются в контейнерах локально (`./compose up`).

### Шаг 2. Быстрый старт
`Dockerfile.api` / `Dockerfile.scheduler`:
- multi-stage build на `eclipse-temurin:21-jdk-alpine` (build) → `21-jre-alpine`
  (runtime). Базовый рантайм-образ ≈ 90 МБ;
- **layered jar** — зависимости / spring-boot-loader / snapshot / application
  копируются разными слоями. Изменение прикладного кода не инвалидирует
  огромный слой с зависимостями;
- JVM-флаги для быстрого старта: `-XX:TieredStopAtLevel=1`,
  `-XX:+UseContainerSupport`, `-XX:MaxRAMPercentage=75.0`,
  `-XX:+ExitOnOutOfMemoryError` (fail-fast при OOM → swarm перезапустит);
- запуск **через `tini`** (PID 1), чтобы SIGTERM/SIGINT корректно доходили
  до JVM и не «съедались» оболочкой;
- образ запускается под непривилегированным пользователем `app`.

### Шаг 3. Сигналы завершения
`application.yaml` обоих модулей:
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: ${SHUTDOWN_TIMEOUT:25s}
```
Поведение при SIGTERM/SIGINT:
1. `GracefulShutdownConfig` ловит `ContextClosedEvent` и **сразу** переводит
   readiness в `REFUSING_TRAFFIC`. `/actuator/health/readiness` возвращает
   **503 Service Unavailable** новым клиентам / health-чекерам.
2. Tomcat перестаёт принимать новые соединения.
3. В пределах `timeout-per-shutdown-phase` (25 s) даём дойти уже выполняющимся
   запросам.
4. Spring закрывает HikariCP, Redis connection-pool, JPA EntityManagerFactory.
5. JVM завершает процесс.

`stop_grace_period` в compose/swarm = 30 s — заведомо больше
`SHUTDOWN_TIMEOUT`, чтобы Docker не прислал SIGKILL раньше, чем Spring закончит
дренаж.

### Шаг 4. Тестирование (compose)

```bash
# Поднять стек
./compose up -d --build

# В одном окне — генератор нагрузки (50 параллельных пользователей, 30 секунд)
docker run --rm --network host williamyeh/hey \
  -z 30s -c 50 http://localhost:8080/actuator/health

# В другом окне — отправляем SIGTERM по одному API-контейнеру
docker kill --signal=SIGTERM tasktracker-api-1

# Что должно произойти:
#   * в логах api: "SIGTERM/SIGINT received — flipping readiness to REFUSING_TRAFFIC"
#   * /actuator/health/readiness → 503
#   * уже идущие запросы завершаются успешно (200)
#   * новые запросы через nginx → 503 (nginx перебрасывает на другую реплику,
#     либо отдаёт 503, если реплик 1)
#   * контейнер завершается чисто, без потерянных запросов
```

Проверить, что readiness отдает 503 во время дренажа:
```bash
docker kill --signal=SIGTERM tasktracker-api-1 &
while true; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    http://localhost:8080/actuator/health/readiness
  sleep 0.2
done
# Ожидаемый вывод: 200, 200, ... 503, 503, (connection refused)
```

### Шаг 5. «Утилизируемость» — Docker Swarm

Конфигурация Swarm в [`docker-stack.yml`](../docker-stack.yml).

**Bootstrap (один раз):**
```bash
./scripts/swarm-bootstrap.sh   # генерирует секреты, инициализирует swarm
```

**Deploy:**
```bash
docker stack deploy -c docker-stack.yml tasktracker
```

**Зачем именно так:**
- `update_config: order: start-first` — новая реплика поднимается **до** того,
  как старая получит SIGTERM. Пока новая проходит `healthcheck`, старая
  продолжает принимать трафик. После того как новая healthy, старая получает
  SIGTERM и дренируется (см. шаг 3).
- `healthcheck` бьёт в `/actuator/health/readiness`. Пока приложение
  стартует — реплика unhealthy и swarm не маршрутизирует на неё трафик.
- `failure_action: rollback` — если новая реплика не стала healthy, swarm
  откатывается на предыдущий образ автоматически.
- `restart_policy: condition: any` — если контейнер упал (OOM, exception),
  swarm поднимет новый. Combined с `-XX:+ExitOnOutOfMemoryError` это даёт
  «cattle, not pets»: процесс умирает чисто → swarm заменяет.
- Состояние **не теряется**, потому что:
  - Postgres-том persistent (`volumes: pgdata`);
  - JWT/secrets — через `docker secret` (`/run/secrets/...`), Spring читает их
    через `spring.config.import: configtree:/run/secrets/`;
  - в самой Java-апликации **нет** in-process state, привязанного к конкретной
    реплике.

**Тест rolling deploy под нагрузкой:**
```bash
# Нагрузка
docker run --rm --network host williamyeh/hey \
  -z 60s -c 100 http://<swarm-node>:8080/actuator/health

# В другом окне — катим новую версию
IMAGE_TAG=<new-sha> docker stack deploy -c docker-stack.yml tasktracker
docker service ps tasktracker_api   # видно, как новые реплики Running, старые Shutdown

# Что должно произойти:
#   * нагрузка — 0 ошибок 5xx (либо retried nginx-ом на здоровую реплику)
#   * в логах старых реплик — "REFUSING_TRAFFIC" + drain
#   * новые реплики становятся healthy и принимают трафик
```

**Тест внезапной потери ноды:**
```bash
docker service ps tasktracker_api      # выбрать одну реплику
docker kill --signal=SIGKILL <cid>     # эмулируем падение

# Swarm заметит unhealthy и через restart_policy поднимет новую реплику.
# При SIGKILL дренажа НЕ происходит — поэтому работает только потому, что
# на других репликах nginx переретраит идущий запрос (proxy_next_upstream).
# При SIGTERM (нормальное завершение) дренаж выполняется штатно.
```

## Краткая шпаргалка

| Что | Где |
|-----|-----|
| Graceful shutdown в Spring | `application.yaml` → `server.shutdown: graceful` |
| Хук на ContextClosedEvent → REFUSING_TRAFFIC | `config/GracefulShutdownConfig.java` |
| Layered jar + tini | `Dockerfile.api`, `Dockerfile.scheduler` |
| Pinned image versions | `docker-compose.yml`, `docker-stack.yml` |
| Healthchecks | `docker-compose.yml`, `docker-stack.yml` |
| Rolling deploy с zero downtime | `docker-stack.yml` → `update_config: order: start-first` |
| Секреты | `./scripts/swarm-bootstrap.sh` + `configtree:/run/secrets/` |
