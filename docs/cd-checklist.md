# CD: что нужно для полного цикла `code → testing → prod`

> Этот документ — чек-лист **«что должно существовать, чтобы пайплайн работал»**.
> Сам пайплайн описан в [`.github/workflows/ci.yml`](../.github/workflows/ci.yml),
> модель окружений — в [`docker-stack.yml`](../docker-stack.yml).

## Модель в одну картинку

```
   push main           push tag v*           workflow_dispatch
       │                    │                       │
       ▼                    ▼                       ▼
   ┌─────────┐         ┌─────────┐             ┌────────────┐
   │  build  │         │  build  │             │  (no build,│
   │ + push  │         │ + push  │             │   reuses)  │
   │ :sha-…  │         │  :v1.2  │             │            │
   │ :latest │         │  :sha-… │             │            │
   │ :main   │         └─────────┘             │            │
   └────┬────┘              │                  │            │
        ▼                   ▼                  ▼            │
  ┌──────────────┐    ┌──────────────┐                       │
  │deploy-testing│    │  deploy-prod │ ◀─────────────────────┘
  │  (auto)      │    │ (approval)   │
  └──────┬───────┘    └──────┬───────┘
         │ pulls :sha-…       │ pulls :v1.2 ИЛИ :sha-<promoted>
         ▼                    ▼
   ┌──────────┐         ┌──────────┐
   │ TEST VM  │         │ PROD VM  │
   │ swarm    │         │ swarm    │
   └──────────┘         └──────────┘
```

**Ключевой принцип**: prod **не пересобирает** образ. Он pull-ит тот же
артефакт, который CI собрал и который уже отработал в testing.

---

## Что нужно настроить **в GitHub** (один раз)

### 1. Secrets (Settings → Secrets and variables → Actions → Repository secrets)

| Secret | Назначение |
|---|---|
| `TESTING_HOST` | IP / DNS test-VM |
| `TESTING_SSH_USER` | юзер с правом запуска `docker` (например, `deploy`) |
| `TESTING_SSH_KEY` | приватный SSH-ключ этого юзера (полный PEM) |
| `PROD_HOST` | IP / DNS prod-VM |
| `PROD_SSH_USER` | то же для прода |
| `PROD_SSH_KEY` | то же для прода |

### 2. Environments (Settings → Environments)

- **`testing`** — без protection rules. Деплой автоматический на push в `main`.
- **`production`** — обязательно:
  - **Required reviewers** — себя + 1 (или хотя бы себя). Без approval prod не катится.
  - **Deployment branches** — `Selected branches and tags` → `v*`, `main`.
  - (опционально) **Wait timer** — задержка 5 мин до старта, чтобы можно было отменить.

### 3. Branch protection на `main`

Settings → Branches → branch protection rule for `main`:
- Require pull request before merging.
- Require status checks: `build`.
- Disallow force-push.

Это страхует от `git push --force` через main, который пробил бы зелёный build
до того, как код реально приехал.

### 4. ghcr.io packages — visibility & permissions

- ghcr.io/<owner>/task-tracker-api: оставить **private** (если код приватный).
- На prod-VM понадобится **PAT с `read:packages`** для `docker login ghcr.io`.

---

## Что нужно настроить **на каждой VM** (один раз)

```bash
# 1. system user для деплоя
sudo useradd -m -s /bin/bash deploy
sudo usermod -aG docker deploy
sudo mkdir -p /home/deploy/.ssh
sudo cp <твой_public_key.pub> /home/deploy/.ssh/authorized_keys
sudo chown -R deploy:deploy /home/deploy/.ssh
sudo chmod 700 /home/deploy/.ssh && sudo chmod 600 /home/deploy/.ssh/authorized_keys

# 2. docker (если ещё нет)
curl -fsSL https://get.docker.com | sh

# 3. логин в ghcr (PAT с read:packages)
sudo -u deploy bash -lc 'echo "$GHCR_TOKEN" | docker login ghcr.io -u <github_user> --password-stdin'

# 4. репозиторий
sudo mkdir -p /opt/tasktracker && sudo chown deploy:deploy /opt/tasktracker
sudo -u deploy git clone https://github.com/qwerdsa53/taskTracker.git /opt/tasktracker
cd /opt/tasktracker

# 5. секреты + swarm
sudo -u deploy ./scripts/swarm-bootstrap.sh

# 6. env-файл для этого окружения
#    test VM:
sudo -u deploy tee .env.testing >/dev/null <<EOF
API_REPLICAS=2
DB_NAME=tasktracker
SERVER_PORT=8080
APP_MAIL_ENABLED=false
EOF
#    prod VM:
sudo -u deploy tee .env.prod >/dev/null <<EOF
API_REPLICAS=3
DB_NAME=tasktracker
SERVER_PORT=80
APP_MAIL_ENABLED=true
EOF

# 7. первый деплой (чтобы overlay-сеть tasktracker_backend появилась —
#    дальше migrate-контейнер в неё сможет подключаться)
export IMAGE_TAG=latest
set -a; . ./.env.testing; set +a    # на prod: .env.prod
sudo -u deploy docker stack deploy -c docker-stack.yml --with-registry-auth tasktracker

# 8. (только prod) бэкапы
sudo mkdir -p /var/backups/tasktracker && sudo chown deploy:deploy $_
```

**Файрвол:**
- testing — `22`, `80` (или `8080`).
- prod — `22`, `80`, `443`. Никаких `5432` / `6379` / `8080` наружу.

---

## Что реально происходит при `git push origin main`

```
1.  build job
    • gradle test
    • docker buildx → ghcr push
        ghcr.io/.../task-tracker-api:sha-<long>
        ghcr.io/.../task-tracker-api:main
        ghcr.io/.../task-tracker-api:latest

2.  deploy-testing job  (environment: testing → нет approval)
    • SSH на test-VM
    • git pull (актуальный docker-stack.yml)
    • docker run --rm ...:sha-<long> migrate    ← 12-factor admin process
    • docker stack deploy (rolling, start-first + healthcheck)
    • 30 раз curl /actuator/health/readiness; если красное → rollback

3.  ...QA на test.example.com...

4.  Промоут в prod — два пути:

    А) Workflow dispatch (РЕКОМЕНДУЕТСЯ — exact same image):
       Actions → CI → Run workflow → promote_sha = <git sha из (1)>
       • environment: production → approval gate
       • prod-VM pull-ит :sha-<длинный> (тот же, что в testing)
       • тот же migrate → stack deploy → smoke check

    Б) Tag-based release:
       $ git tag v1.4.2 && git push --tags
       • build job соберёт :v1.4.2 / :sha-<...>
       • deploy-prod ждёт approval → pull :v1.4.2 → migrate → deploy
```

**Откат на проде** (без CI, прямо на VM):
```bash
# на prod-VM
docker service update \
  --image ghcr.io/qwerdsa53/task-tracker-api:sha-<previous> \
  tasktracker_api
docker service update \
  --image ghcr.io/qwerdsa53/task-tracker-scheduler:sha-<previous> \
  tasktracker_scheduler
```

Тот же rolling-механизм (`order: start-first` + healthcheck), zero downtime.
Старый образ всё ещё в ghcr — pull занимает секунды.

---

## Чего ещё не хватает (хорошо бы добавить)

В порядке убывания пользы:

1. **Karate / smoke-тесты в `deploy-testing`** перед тем как считать testing «зелёным».
   У тебя уже есть `karate-e2e-infra.sh` — можно прогнать его SSH-командой
   против `http://localhost/api/...` после деплоя. Если падает — testing
   считается провалившимся, прод-promote блокируется.

2. **Slack / TG webhook** в `success/failure` каждого job-а — `appleboy/telegram-action`
   или `slackapi/slack-github-action`. Без алерта о провале деплоя ты узнаешь
   утром из багов от пользователей.

3. **Метрики и логи**. У тебя уже есть `/actuator/prometheus`. Поднять рядом
   с приложением `prometheus + grafana + loki` в том же swarm-стеке (или один
   контейнер `grafana/agent` → внешний Grafana Cloud). Без дашбордов оценить
   «у нас всё ок?» = guess.

4. **DB-миграция backwards-compatible**. Сейчас migrate бежит **до** старта
   новой версии и **после** того, как старые реплики ещё живы. Значит:
   старая версия должна работать с новой схемой. Правило: миграция = только
   add column / new table / new index (CONCURRENTLY). Никаких `DROP` /
   `RENAME` в одном релизе — разнести на два релиза с expand/contract.

5. **Image scanning**. `trivy-action` в build job — блокировать pushed image,
   если в нём CVE с severity ≥ HIGH.

6. **Сжатие docker layers** — у тебя уже layered jar, но base image ещё ~90 МБ.
   Можно перейти на `gcr.io/distroless/java21-debian12:nonroot` — ~25 МБ
   и без `apk`/shell вообще. Но это меняет debug-возможности (`shell` verb
   в `app.sh` перестанет работать) — взвесь.

7. **`docker stack deploy --prune`** — чтобы при удалении сервиса из стека он
   физически удалялся. По умолчанию stale services остаются.

8. **Postgres backup в S3 / R2**, а не на ту же VM (что мы сейчас делаем
   в `pg-backup.sh`). Если VM умирает целиком — бэкапы на ней не помогают.

---

## Краткий итог: «что нужно»

| Категория | Что | Где |
|---|---|---|
| GitHub | secrets × 6, environments × 2, branch protection | settings UI |
| testing VM | docker, deploy user, repo, swarm-bootstrap, `.env.testing`, первый stack deploy | bash, один раз |
| prod VM | то же + бэкап dir + ужесточённый firewall | bash, один раз |
| Код | tag-based image tags (метадата), promote workflow, migrate-gate, smoke-check, rollback-on-fail | уже в [`ci.yml`](../.github/workflows/ci.yml) |
| Operations | бэкап перед prod deploy, ручной rollback тегом | [`scripts/pg-backup.sh`](../scripts/pg-backup.sh) |

Всё в коде из этого пункта уже сделано. Дальше — настроить GitHub secrets +
поднять VM по чек-листу выше, и `git push` поедет в testing автоматически.
