# Proposal

## Why

The `task-management` spec requires a not-found error whenever a caller references a task that does not exist, but the implementation does not honor that requirement. The spec was authored as intended behavior, so the gap is in the code, not the spec — this change closes it. Getting this right now matters because the other planned changes build on the same error contract, and a 500 for an ordinary client mistake makes every later debugging session noisier.

## What Changes

- Introduce a dedicated not-found exception and a global handler that maps it to an HTTP 404 response.
- Replace the `IllegalArgumentException` that `TaskService.findById` throws for an absent id, which currently surfaces as a 500 and is also semantically wrong (an absent row is not an illegal argument).
- Add an explicit existence check to `TaskService.delete`. This is a separate defect from the exception: `TaskRepository.deleteById` is a silent no-op for an absent id in Spring Data JPA 3.x, so deleting a missing task currently returns 204 instead of 404.
- Add backend tests covering the not-found contract for the retrieve, update, and delete endpoints. The module currently has no test sources at all, so this change introduces the first ones.

Endpoint behavior before and after:

| Endpoint | Absent id today | After |
| --- | --- | --- |
| `GET /api/tasks/{id}` | 500 | 404 |
| `PUT /api/tasks/{id}` | 500 | 404 |
| `DELETE /api/tasks/{id}` | 204 | 404 |

Success responses are unchanged, including the 204 that a successful delete already returns.

## Capabilities

### New Capabilities

None. This change introduces no new capability.

### Modified Capabilities

None. `task-management` already specifies the not-found behavior; the code is being brought into line with it rather than the requirement being redefined. This change therefore sets `skip_specs: true` in `.openspec.yaml` and writes no delta spec.

## Impact

- Code: `TaskService`, `TaskController`, a new exception type, and a new `@RestControllerAdvice` handler.
- Tests: a new `src/test` tree in the backend module.
- Dependencies: none added. `spring-boot-starter-test` is already declared in `backend/pom.xml` but currently unused.
- API: the status code for an absent task id changes from 500 or 204 to 404. The Angular client does not currently inspect error responses, so no client change is required; a client that starts handling these errors will see 404 rather than 500.
- Out of scope: blank-title validation, edit prefill, DTO extraction, and the error response body shape.
