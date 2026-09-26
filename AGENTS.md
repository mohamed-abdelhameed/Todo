# AGENTS.md

Todo list app: Spring Boot backend + Angular frontend.
Monorepo layout:

```
backend/   Spring Boot 3.3.5 (Java 17, Maven)
frontend/  Angular 17 (standalone components, npm)
```

## Backend (Spring Boot)

- **Build**: `mvn -q compile`
- **Run**: `mvn spring-boot:run` (starts on port **8080**)
- **Stack**: Spring Boot 3.3.5, Java 17, Spring Web, Spring Data JPA, Flyway, H2 (in-memory `tododb`, default profile), Lombok, validation
- **Schema**: owned by Flyway. Migrations live in `backend/src/main/resources/db/migration/` as `V<version>__<name>.sql` and are applied in version order at startup; applied migrations are recorded in `flyway_schema_history`.
- **Schema changes**: add a new numbered migration for every change. Never edit a migration that has already been applied — Flyway refuses to start on a checksum mismatch.
- **Hibernate DDL generation**: off. `spring.jpa.hibernate.ddl-auto=validate` makes Hibernate only verify the migrated schema against the entities and fail startup on drift; it never creates or alters tables.
- **PostgreSQL**: `mvn spring-boot:run -Dspring-boot.run.profiles=postgres` (from `backend/`). Datasource comes from `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`; local-dev defaults live in `application-postgres.properties`. The same migrations run on H2 and PostgreSQL.
- **Adopting on a database that already has tables**: Flyway refuses a non-empty schema that has no history table. Either drop the database and let the baseline create it, or run once with `--spring.flyway.baseline-on-migrate=true` deliberately. Never enable it by default.
- **REST API** (`backend/src/main/java/com/example/todo/web/TaskController.java`):
  - `GET    /api/tasks`            – list all, optional `?completed=true|false` filter
  - `GET    /api/tasks/{id}`       – get one; unknown id → **404** (empty body)
  - `POST   /api/tasks`            – create
  - `PUT    /api/tasks/{id}`       – update; unknown id → **404** (empty body)
  - `DELETE /api/tasks/{id}`       – delete; unknown id → **404** (empty body)
- **Not-found mapping**: `TaskNotFoundException` (service package) is mapped to 404 with an empty body by `GlobalExceptionHandler`; the handler declares no catch-all, so unrelated faults still surface as 500.
- **Tests**: `mvn -q test` — 14 tests. Most are MockMvc + H2 with `@Transactional` rollback; `DatabaseProfileConfigTest` is plain JUnit, `SchemaMigrationTest` asserts the Flyway history and the `tasks` shape, and `SchemaDriftTest` deliberately asserts that a context **fails** to start.
- **CORS**: allows `http://localhost:4200` (configured in `application.properties`)

## Frontend (Angular)

- **Install deps**: `npm install` (in `frontend`)
- **Dev server**: `npm start` (Angular CLI dev server on port **4200**)
- **Build**: `npm run build` (output to `frontend/dist`)
- **Stack**: Angular 17.3.9, standalone components (bootstrapApplication), Angular Material not used; plain CSS
- **API client**: `frontend/src/app/services/task.service.ts` – base URL `http://localhost:8080/api/tasks`
- **Key components**:
  - `task-list` – list, toggle complete, filter (all/active/completed), edit, delete
  - `task-form` – add & edit (via `editingTask` input)
- **Models**: `frontend/src/app/models/task.model.ts` (`Task` interface)
- **Filter type**: `frontend/src/app/shared/task-filter.ts` (`TaskFilter = 'all' | 'active' | 'completed'`)

## Environment quirks (Windows / Git Bash)

- Node/npm are **not** on PATH inside Git Bash. Use the full path to the active version, e.g.:
  ```
  export NODE="/c/Users/MohamedAbdelhameed/.nvm/versions/node/v22.23.2/bin/node.exe"
  "$NODE" node_modules/@angular/cli/bin/ng.js build
  ```
- Angular CLI is **not** installed globally. Use the local `node_modules/@angular/cli/bin/ng.js` (or `npm start`).
- Maven works normally from Git Bash.
- PostgreSQL 16 runs inside the **Ubuntu WSL** distro (not Windows, not Docker) and is reachable from Windows on `localhost:5432`; the scratch `todo` database used to verify the `postgres` profile lives there. Administering it (`sudo -u postgres psql`) prompts for the WSL password, so non-interactive runs must supply `POSTGRES_PASSWORD` instead.
- `ng lint` is defined but requires installing `@angular-eslint/*` first (not yet installed).

## Notes

- Git repo not yet initialized at `D:\TrainingProjects\Todo`.
- Original repo was empty; backend + frontend were scaffolded from scratch.
- Per `~/.claude/CLAUDE.md`, tech stack also includes Flutter (mobile) for future work.
