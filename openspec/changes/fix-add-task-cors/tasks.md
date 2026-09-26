## 1. Backend CORS Configuration

- [x] 1.1 Add a `CorsConfig` class under `backend/src/main/java/com/example/todo/config/` implementing `WebMvcConfigurer`, overriding `addCorsMappings` to register `/api/**` with allowed origin `http://localhost:4200` and methods `GET, POST, PUT, DELETE, OPTIONS`; verify it compiles with `mvn -q compile`
- [x] 1.2 Remove the `spring.web.cors.allowed-origins`, `allowed-methods`, and `allowed-headers` lines from `application.properties`; verify no reference to `spring.web.cors` remains

## 2. Verification

- [ ] 2.1 Rebuild and restart the backend, then verify via curl that a `GET /api/tasks` request with `Origin: http://localhost:4200` returns an `Access-Control-Allow-Origin` header
- [ ] 2.2 Verify via curl that an `OPTIONS /api/tasks` preflight (with `Origin`, `Access-Control-Request-Method: POST`, `Access-Control-Request-Headers: content-type`) returns success (not 403) and includes the allow headers
- [ ] 2.3 In the browser against the running Angular dev server, add a task and confirm it appears in the list, and that edit/toggle/delete still work
