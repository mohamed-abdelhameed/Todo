# Design

## Context

See `proposal.md` - Why for motivation. What shapes the approach:

- The not-found condition originates in the service layer, but the requirement is about an HTTP status code. The two layers need a seam between them.
- The delete path has no exception to catch today. `deleteById` in Spring Data JPA 3.3.5 compiles to `findById(id).ifPresent(...)` (verified by disassembling `SimpleJpaRepository`), so the absence is silently ignored rather than raised.
- `TaskService.findById` is the single existing throw site, and it is already the shared lookup for both `getById` and `update`. Fixing it once fixes both endpoints.
- `TaskService.delete` does not call `findById` at all, which is why it needs separate treatment.
- The backend module has no `src/test` tree. `spring-boot-starter-test` is declared in `backend/pom.xml` but unused, so no dependency work is needed to add tests.

## Goals / Non-Goals

**Goals:**
- One place that decides an absent task id means 404, so the retrieve, update, and delete paths cannot drift apart.
- Keep the service layer free of HTTP types.
- Cover the contract with tests that would actually have caught both defects.

**Non-Goals:**
- Any general error-handling framework, error body schema, or problem-details format.
- Blank-title validation and the visible rejection it needs (a separate change).
- DTO extraction or any restructuring of the entity boundary.
- Changing success responses, including the 204 on a successful delete.

## Decisions

**1. Signal not-found with a dedicated exception, mapped by a `@RestControllerAdvice`.**
Chosen: a `TaskNotFoundException` thrown from the service, plus a `GlobalExceptionHandler` that maps exactly that type to 404.
Rejected: throwing `ResponseStatusException` from the service, which is less code but couples the service to `HttpStatus` and spreads the mapping across call sites; returning `Optional<Task>` and letting each controller method unwrap it, which keeps HTTP concerns in the controller but forces every method to repeat the unwrap-and-branch as the API grows.
Rationale for the advice approach: the mapping lives in one file, so the error types added by later changes get a consistent home instead of a new branch per endpoint.

**2. Handler is mapped only to `TaskNotFoundException`, never to `Exception`.**
A catch-all handler would convert every unhandled failure into a response that looks deliberate, trading a visible 500 for an invisible one. Narrow mapping keeps genuine server faults loud.

**3. `delete` resolves the entity through `findById` and then deletes it, rather than checking `existsById` first.**
`findById(id).orElseThrow(...)` then `repository.delete(existing)` reuses the same throw site as the other two paths, costs one query instead of two, and has no check-then-act window in which the row could vanish between the check and the delete.
Cost: a full row load where a delete-only path would not need one. At this scale that is irrelevant, and it is the price of not having two different not-found behaviors.
`existsById` followed by `deleteById` was rejected for the race and for reintroducing a silent no-op if the row disappears between the two calls.

**4. The 404 response carries no body.**
`ResponseEntity.notFound().build()`. The requirement constrains the absence of a task body, not the presence of an error body, so the smallest conforming response is the right one to land now. The body shape is left to whichever change first needs it rather than being fixed here and then changed.

## Risks / Trade-offs

- [A future exception type gets added to the advice and silently swallows server faults] → keep the handler's mappings enumerated and type-specific; add a test that an unrelated runtime failure still yields 500.
- [Replacing `IllegalArgumentException` changes behavior for any caller catching it] → the only throw site in the module is `TaskService.findById`, and nothing catches it, so the blast radius is the three endpoints already in scope.
- [Tests that mock the repository would not have caught the delete defect] → use a full application context against the real H2 database rather than a mocked repository, so the repository's real absent-id behavior is what the test exercises.
- [Entity load on delete costs a redundant SELECT] → accepted; the alternative is a second not-found code path, which is the more expensive mistake.
