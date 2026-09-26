# Tasks

## 1. Test harness

The backend module has no test sources today, so the harness has to be proven before any group can land a test. This group is scaffolding: its work calls for no further tests or documentation.

- [x] 1.1 Add a `@SpringBootTest` + `@AutoConfigureMockMvc` smoke test asserting `GET /api/tasks` returns 200 with a JSON array, and verify `mvn -q test` passes, which proves the test starter resolves, the context loads, and H2 is reachable under test

## 2. Not-found signal and mapping

- [x] 2.1 Add a `TaskNotFoundException` in the service package carrying the absent id, and verify `mvn -q compile` succeeds
- [x] 2.2 Add a `@RestControllerAdvice` that maps `TaskNotFoundException` to 404 with no response body, and verify `mvn -q compile` succeeds
- [x] 2.3 Replace the `IllegalArgumentException` in `TaskService.findById` with `TaskNotFoundException`, and verify `mvn -q compile` succeeds
- [x] 2.4 Add MockMvc tests asserting `GET /api/tasks/{id}` and `PUT /api/tasks/{id}` with an absent id each return 404 with an empty body, and verify both pass under `mvn -q test`
- [x] 2.5 Add a test asserting no declared `@ExceptionHandler` on the advice maps `Exception` or `Throwable`, and verify it passes under `mvn -q test`

## 3. Delete not-found

- [x] 3.1 Resolve the entity through `findById` in `TaskService.delete` and delete the resolved entity, and verify `mvn -q compile` succeeds
- [x] 3.2 Add a MockMvc test asserting `DELETE /api/tasks/{id}` with an absent id returns 404, and verify it passes under `mvn -q test`
- [x] 3.3 Add a MockMvc test asserting `DELETE /api/tasks/{id}` with an existing id still returns 204 and the task is absent from a subsequent list, and verify it passes under `mvn -q test`
- [x] 3.4 Record the not-found contract in the REST API section of `AGENTS.md`, and verify each of the three endpoint lines states that an unknown id yields 404
