# Spec Delta

## Purpose

Defines how the database schema is created and evolved: versioned, ordered migration scripts own every schema change, the applied set is recorded, and startup fails loudly when the schema and the application's entity mapping disagree, on every database the system supports.

## ADDED Requirements

### Requirement: Schema is created and evolved only by versioned migrations

The system SHALL create and upgrade its database schema by applying ordered, versioned migration scripts, and SHALL NOT create or alter tables through the persistence layer's own schema generation. Each migration SHALL be applied at most once, and migrations the database has already recorded as applied SHALL be skipped on later startups.

#### Scenario: First startup against an empty database
- **WHEN** the application starts against a database that holds no schema
- **THEN** every pending migration is applied in version order
- **AND** the resulting tables match the entity mappings
- **AND** the application serves requests successfully

#### Scenario: Restart against a database that is already migrated
- **WHEN** the application starts against a database that has already applied the migrations
- **THEN** no migration script is executed again
- **AND** the startup does not fail because the tables already exist
- **AND** the application serves requests successfully

### Requirement: Applied migrations are recorded in the database

The system SHALL maintain a migration history inside the database that records which migrations have been applied and whether they succeeded, so the schema version in use is inspectable from the database itself rather than inferred from code.

#### Scenario: History after a successful startup
- **WHEN** the application has started successfully against an empty database
- **THEN** the migration history lists the baseline migration as successfully applied
- **AND** the history records it as applied exactly once

#### Scenario: History grows with each new migration
- **WHEN** a new migration is added and the application starts again
- **THEN** the migration history lists the new migration as successfully applied
- **AND** the earlier entries remain unchanged

### Requirement: Startup fails when the entity mapping and the schema disagree

The system SHALL verify the migrated schema against its entity mappings during startup and SHALL refuse to start when they disagree, rather than altering the schema to make the disagreement disappear.

#### Scenario: Entity expects a column no migration creates
- **WHEN** an entity mapping declares a column that the applied migrations do not create
- **THEN** startup fails and reports the mismatch
- **AND** the database schema is left exactly as the migrations left it

#### Scenario: Entity mapping matches the migrations
- **WHEN** the applied migrations produce a schema that satisfies every entity mapping
- **THEN** the verification succeeds
- **AND** startup continues normally

### Requirement: One migration set applies to every supported database

The system SHALL apply the same ordered set of migrations to every database it supports, so that a schema proven in the development database is reproduced identically elsewhere rather than being redefined per environment.

#### Scenario: Same baseline applied to each supported database
- **WHEN** the application starts against the in-memory development database and then against the PostgreSQL database
- **THEN** both databases receive the same migration scripts in the same order
- **AND** the resulting task table has the same columns, types, nullability, and primary key in both

#### Scenario: PostgreSQL is unreachable
- **WHEN** the application starts with the PostgreSQL database selected and the database cannot be reached
- **THEN** startup fails and reports the connection failure
- **AND** the application does not start against a fallback database

### Requirement: Applied migrations are immutable

The system SHALL refuse to start when a migration that a database has already recorded as applied has been modified afterwards, so that environments cannot diverge behind an unchanged migration number.

#### Scenario: An already-applied migration is edited
- **WHEN** a migration script that a database has already applied is edited
- **THEN** startup fails and reports that the applied migration no longer matches
- **AND** the application does not proceed against the altered script

#### Scenario: A migration is appended rather than edited
- **WHEN** a new migration with a higher version is added and earlier migrations are left untouched
- **THEN** only the new migration is applied
- **AND** startup succeeds
