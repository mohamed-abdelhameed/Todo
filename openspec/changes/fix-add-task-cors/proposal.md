## Why

In the browser, attempting to add a task from the Angular frontend (port 4200) against the Spring Boot backend (port 8080) does nothing: the task never appears. The endpoint logic works (verified via curl), but the backend emits no CORS headers, so the browser silently blocks every cross-origin response. The intended CORS config in `application.properties` (`spring.web.cors.*`) is not being applied.

## What Changes

- Emit correct CORS headers on the backend so the Angular origin (`http://localhost:4200`) can call the REST API.
- Handle CORS preflight (`OPTIONS`) requests so `POST`, `PUT`, `DELETE` (and `GET`) succeed from the browser.
- Replace the non-functional `spring.web.cors.*` properties with a mechanism that is actually applied at runtime.

## Capabilities

### New Capabilities
- `cors`: Cross-origin resource sharing for the task REST API, allowing the Angular frontend origin to call `/api/**` with the required methods and headers, including correct preflight handling.

### Modified Capabilities
<!-- No existing spec-level behavior changes: openspec/specs/ is empty and no capability spec exists yet. -->

## Impact

- Backend only (`backend/src/main/java/com/example/todo/...` and `application.properties`).
- Adds a small Spring `@Configuration` class implementing `WebMvcConfigurer` (or a `CorsConfigurationSource` bean) to register CORS mappings.
- `application.properties`: the `spring.web.cors.*` lines are removed (they are a no-op) or reconciled with the new config.
- No change to the frontend (`task.service.ts` base URL and calls already target `http://localhost:8080/api/tasks`).
- No change to the task domain model, repository, or service.
