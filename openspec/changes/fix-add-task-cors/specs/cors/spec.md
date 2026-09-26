## Purpose

Allows the Angular frontend served from `http://localhost:4200` to call the todo REST API across origins, so browser requests are no longer silently blocked by CORS.

## ADDED Requirements

### Requirement: CORS allowed origin for the Angular frontend
The REST API SHALL include CORS response headers that allow the origin `http://localhost:4200` to access `/api/**` endpoints. The allowed origin MUST match the value configured for the frontend dev server so that the task list load and all CRUD operations succeed from the browser.

#### Scenario: Task list loads from the browser
- **WHEN** the frontend at `http://localhost:4200` performs a GET request to `/api/tasks` with an `Origin: http://localhost:4200` header
- **THEN** the response includes `Access-Control-Allow-Origin` permitting that origin
- **AND** the browser can read and render the returned task list

### Requirement: CORS preflight requests are accepted
The REST API SHALL accept CORS preflight `OPTIONS` requests for the configured origin and return the appropriate `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`, and `Access-Control-Allow-Origin` headers, so that non-simple requests (`POST`, `PUT`, `DELETE`) are not blocked before they are sent.

#### Scenario: Add task POST preflight succeeds
- **WHEN** the frontend at `http://localhost:4200` sends an `OPTIONS` preflight for `POST /api/tasks` with `Access-Control-Request-Method: POST` and `Access-Control-Request-Headers: content-type`
- **THEN** the API responds with a success status
- **AND** the response includes `Access-Control-Allow-Methods` containing `POST`
- **AND** the response includes `Access-Control-Allow-Origin` permitting `http://localhost:4200`

#### Scenario: Update and delete preflights succeed
- **WHEN** the frontend at `http://localhost:4200` sends `OPTIONS` preflights for `PUT /api/tasks/{id}` and `DELETE /api/tasks/{id}`
- **THEN** the API responds with a success status for each
- **AND** each response permits the origin and includes the requested method

### Requirement: CORS restriction to the configured origin
The REST API SHALL NOT grant cross-origin access to unconfigured origins; only `http://localhost:4200` is allowed.

#### Scenario: Unconfigured origin is not allowed
- **WHEN** a request to `/api/tasks` includes an `Origin` header that is not `http://localhost:4200`
- **THEN** the response does not include an `Access-Control-Allow-Origin` header that grants that origin access
