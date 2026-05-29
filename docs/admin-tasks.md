# Admin processes (12-factor, фактор XII)

Одноразовые задачи (миграции, создание админа, очистка кеша) запускаются как
**короткоживущие контейнеры из того же образа**, что и сам сервис.

> Принцип: ничего не меняем «руками» внутри работающего контейнера. Любая
> административная операция — это `docker run --rm ...` идентичного образа с
> другой командой.

## Архитектура

```
                    ghcr.io/qwerdsa53/task-tracker-api:<sha>
                              │
                ┌─────────────┴─────────────┐
                │                           │
        ENTRYPOINT /app/app                 │
                │                           │
   ┌────────────┼────────────┬──────────────┼────────┐
   ▼            ▼            ▼              ▼        ▼
 server      migrate    create-admin   cache-clear  shell
(default)    Flyway     UserRepo+Bcrypt  Redis FLUSHDB  debug
long-lived   exit 0/1    exit 0/1        exit 0/1
```

Wrapper [`docker/app.sh`](../docker/app.sh) — единственная точка входа. Он:
- по умолчанию запускает HTTP-сервис (`CMD ["server"]` в Dockerfile);
- для admin-команд выставляет `spring.main.web-application-type=none` →
  порт не открывается, JVM завершается сразу после выполнения задачи;
- передаёт CLI-флаги (`--email=...`) внутрь Spring, где их разбирает
  [`AdminTaskRunner`](../task-tracker-api/src/main/java/edu/mirea/qwerdsa53/taskTracker/admin/AdminTaskRunner.java).

## Доступные команды

| Команда | Что делает | Аргументы |
|---|---|---|
| `server` (default) | Поднимает HTTP-сервис | — |
| `migrate` | Flyway применяет миграции, exit 0 | — |
| `create-admin` | Создаёт пользователя `email_verified=true` | `--email=` `--password=` `[--username=]` `[--timezone=]` |
| `cache-clear` | `FLUSHDB` на Redis | — |
| `shell` | `/bin/sh` — debug only | — |
| `help` | Справка | — |

## Шаг 4: ручной запуск

**Создать админа** — без `docker exec` и захода внутрь:

```bash
docker run --rm \
  --network tasktracker_backend \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=postgres -e DB_PORT=5432 -e DB_NAME=tasktracker \
  -e DB_USER="$(cat /opt/tasktracker/.secrets/db_user.txt)" \
  -e DB_PASSWORD="$(cat /opt/tasktracker/.secrets/db_password.txt)" \
  ghcr.io/qwerdsa53/task-tracker-api:latest \
  create-admin --email=admin@example.com --password='S3cr3t!' --username=admin
```

Контейнер запустился, выполнил задачу, exit-код = 0 — и удалил себя
(`--rm`). На сервере не осталось ничего.

**Очистить Redis:**
```bash
docker run --rm \
  --network tasktracker_backend \
  -e REDIS_HOST=redis -e REDIS_PORT=6379 \
  ghcr.io/qwerdsa53/task-tracker-api:latest cache-clear
```

**Накатить миграции вручную** (например, hot-fix перед деплоем):
```bash
docker run --rm \
  --network tasktracker_backend \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=postgres -e DB_PORT=5432 -e DB_NAME=tasktracker \
  -e DB_USER="$(cat /opt/tasktracker/.secrets/db_user.txt)" \
  -e DB_PASSWORD="$(cat /opt/tasktracker/.secrets/db_password.txt)" \
  ghcr.io/qwerdsa53/task-tracker-api:<sha> migrate
```

## Шаг 3: миграции в CI/CD как pre-deploy gate

Job `deploy-testing` / `deploy-prod` в [`ci.yml`](../.github/workflows/ci.yml):

```yaml
docker run --rm --network tasktracker_backend \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=postgres -e DB_PORT=5432 -e DB_NAME=tasktracker \
  -e DB_USER=... -e DB_PASSWORD=... \
  ghcr.io/.../task-tracker-api:${IMAGE_TAG} migrate
```

`set -euo pipefail` в начале SSH-скрипта гарантирует: если этот шаг падает
с ненулевым кодом — **`docker stack deploy` НЕ выполняется**. Кластер
остаётся на старой версии, в логах CI — причина из Flyway.

## Шаг 5: полный цикл 12 факторов

```
1. Изменение кода (локально)
   $ git checkout -b fix-typo
   $ git commit && git push

2. PR → CI (build job)
   • ./gradlew test
   • docker build → ghcr.io/.../task-tracker-api:<sha>
   • docker build → ghcr.io/.../task-tracker-scheduler:<sha>

3. Merge to main → deploy-testing job
   а) Pull image на testing VM
   б) ОДНОРАЗОВЫЙ контейнер: docker run --rm ...:<sha> migrate
      • если упал — pipeline FAILED, на testing старая версия
   в) docker stack deploy (rolling start-first):
      • новая реплика стартует
      • healthcheck readiness прошёл → swarm переключает трафик
      • старая реплика получает SIGTERM → graceful drain (25 s)
      • повторить для каждой реплики (parallelism: 1)
   г) Smoke check: 30 раз curl /actuator/health/readiness
      • если за минуту не 200 → docker service rollback автоматически

4. Manual QA / canary / k6 на testing.example.com

5. Релиз в прод
   $ git tag v1.4.2 && git push --tags
   → deploy-prod job ждёт approval в GitHub Environments (production)
   → approve → SSH на prod VM → migrate → stack deploy

6. Откат: тегом образа, без redeploy / без rebuild
   $ ssh prod
   $ cd /opt/tasktracker
   $ docker service update --image \
        ghcr.io/qwerdsa53/task-tracker-api:v1.4.1 tasktracker_api
   # swarm катит rolling rollback с теми же гарантиями (start-first + healthcheck)
```

### Почему откат тегом, а не git revert + rebuild

- образ уже собран и лежит в реестре — pull занимает секунды;
- никаких новых SQL-миграций при откате (миграции должны быть
  backwards-compatible на одну версию назад — это отдельное правило);
- `docker service update` идёт через тот же `update_config: start-first`,
  что и обычный деплой, → zero downtime.

## Идемпотентность

- **migrate** — Flyway сам идемпотентен (`flyway_schema_history` хранит
  применённые скрипты). Повторный запуск = no-op.
- **create-admin** — проверяет `existsByEmail` перед вставкой, exit 1
  если пользователь уже есть. Не апсертит — намеренно, чтобы случайно не
  затереть пароль.
- **cache-clear** — `FLUSHDB` идемпотентен по определению.

## Чего здесь специально нет

- **Нет `docker exec` в production-сценариях.** Только `docker run --rm`.
  Это разница между «административная задача как процесс» и «изменение
  состояния работающего контейнера».
- **Нет sidecar-контейнеров для миграций**, синхронно запущенных в стеке
  на каждый старт. Миграция должна выполниться **один раз** до деплоя, а
  не N раз параллельно из всех новых реплик.
- **Нет `kubectl exec` /отдельного «migration job» yaml**. Тот же образ,
  тот же entrypoint, разный verb — этого достаточно.
