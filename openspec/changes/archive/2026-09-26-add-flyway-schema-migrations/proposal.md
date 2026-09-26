# Proposal

## Why

The backend currently has no versioned schema: `spring.jpa.hibernate.ddl-auto=update` lets Hibernate invent and mutate tables from the entity mapping at every startup. The resulting DDL is implicit, unreviewable, and unrecorded, so nobody can tell what the schema is meant to be, whether it changed, or whether an entity and the database have drifted apart. Adopting Flyway with an explicit baseline script turns the schema into a reviewable, versioned artifact, and it is the prerequisite for running the same migrations against a real PostgreSQL database.

## What Changes

- Add Flyway to the backend and let it own all schema creation. Flyway's version is managed by the Spring Boot 3.3.5 parent (no explicit version in the POM).
- Add a baseline migration, `V1__create_tasks_table.sql`, that creates the `tasks` table. It reproduces exactly the DDL Hibernate generates today for the current `Task` entity, so no entity changes and no API changes are needed.
- Switch `spring.jpa.hibernate.ddl-auto` from `update` to `validate`. Hibernate no longer creates or alters anything; it only checks the migrated schema against the entity mapping and fails startup on mismatch.
- Add a `postgres` Spring profile with its own datasource URL, credentials placeholder, and driver, so the same migrations can be applied to PostgreSQL. The default profile keeps using in-memory H2.
- Record the convention that every future schema change ships as a new numbered Flyway migration and that Hibernate DDL generation stays off.
- Cover the new arrangement with tests: the baseline migration is applied on startup, the schema exists, and the existing REST tests keep passing against the Flyway-created table.

## Capabilities

### New Capabilities
- `schema-migrations`: How the database schema is created, versioned, kept consistent with the entity mapping, and applied across the supported databases.

### Modified Capabilities
- None. `task-management` describes task behavior over an unspecified store; nothing about its requirements changes.

## Impact

- **Dependencies**: add `org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql` (Flyway 10 moved PostgreSQL support out of the core jar), and the `org.postgresql:postgresql` runtime driver to `backend/pom.xml`.
- **Configuration**: `backend/src/main/resources/application.properties` changes `ddl-auto` from `update` to `validate`; new `backend/src/main/resources/application-postgres.properties` holds the PostgreSQL datasource.
- **New files**: `backend/src/main/resources/db/migration/V1__create_tasks_table.sql` (baseline) and a schema-management test.
- **Existing behavior**: no REST contract change, no entity change, no frontend change. H2 remains in-memory, so the baseline is re-applied on every boot; that is expected and is what makes the H2 database reproducible.
- **Operational**: startup now fails fast on entity/migration drift instead of silently altering tables. A production deployment gains a migration history table (`flyway_schema_history`) that records what was applied and when.
