## Context

The Spring Boot backend (`backend/`, Boot 3.3.5, Java 17) serves a REST API on `:8080`; the Angular frontend runs on `:4200`. The intended CORS config lives only in `application.properties` via `spring.web.cors.*`. Empirically (tested against the running server) it is a no-op: no `Access-Control-Allow-Origin` header is ever emitted and preflight `OPTIONS` returns 403. Motivation in proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Make the backend emit correct CORS headers for `http://localhost:4200` on `/api/**`.
- Handle preflight so the browser sends through `POST`, `PUT`, `DELETE`.
- Do it with a mechanism that is verifiably applied at runtime.

**Non-Goals:**
- No change to task domain, service, or repository.
- No frontend changes (`task.service.ts` already targets `http://localhost:8080/api/tasks`).
- No other CORS origins or environments beyond the local Angular dev server.

## Decisions

**Decision 1: Use a `WebMvcConfigurer` with `addCorsMappings` (recommended).**
Add a small `@Configuration` class that implements `WebMvcConfigurer` and overrides `addCorsMappings` to register a mapping on `/api/**` allowing origin `http://localhost:4200` and methods `GET, POST, PUT, DELETE, OPTIONS`. This is the approach Spring Boot's own MVC docs recommend for global CORS and is the most reliably applied path for a servlet app. It produces correct headers and preflight handling out of the box.

*Alternative considered:* A `CorsConfigurationSource` bean (via `@Bean`) wired to a `UrlBasedCorsConfigurationSource`. Also valid and roughly equivalent; a `WebMvcConfigurer` was chosen for being the explicit, documented, first-class global mechanism and easier to read/grep.

*Alternative considered:* `@CrossOrigin` on the controller. Rejected: it is per-handler and must be repeated on every future endpoint; it also does not cover the preflight rejection caused by the current no-op config as cleanly. Fine as a stopgap but not the structural fix.

**Decision 2: Remove the non-functional `spring.web.cors.*` properties.**
`application.properties` lines `spring.web.cors.allowed-origins`, `allowed-methods`, `allowed-headers` are stripped to avoid silent-config confusion. The effective policy moves into the configuration class.

*Alternative considered:* Keep the properties and debug why they don't apply. Rejected for this change: they are undocumented for this path in the MVC docs and have already burned effort; the explicit config removes the ambiguity.

**Decision 3: Single source of truth for the allowed origin and methods.**
Both the mapping path (`/api/**`) and allowed values are inlined in the config class. No new dependency is introduced (Spring Web is already present).

## Risks / Trade-offs

- [Locked-in origin string] → The allowed origin is hardcoded to `http://localhost:4200`. If the dev server is later accessed via `127.0.0.1` or a different port, origin matching fails. Consider externalizing the origin as a property if this becomes a concern.
- [Wildcard vs explicit headers] → Using `*` for allowed headers is permissive but fine for an in-memory dev todo app. Restrict later if auth headers appear.
- [Assignment of the mess] → Removing `spring.web.cors.*` is safe (it did nothing), but ensures no duplicate/conflicting CORS beans exist (there are none in this codebase currently).

## Migration Plan

1. Add `CorsConfig` configuration class implementing `WebMvcConfigurer`.
2. Remove the three `spring.web.cors.*` lines from `application.properties`.
3. Rebuild and restart the backend; verify via curl that an `Origin: http://localhost:4200` request returns `Access-Control-Allow-Origin` and that `OPTIONS` preflight returns 200 (not 403).
4. Verify in the browser that adding a task shows up in the list.
