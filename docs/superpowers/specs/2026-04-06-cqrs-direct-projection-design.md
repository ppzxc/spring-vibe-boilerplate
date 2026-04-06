# CQRS Direct Projection Strategy Design

## Objective
Elevate the "Direct Projection" pattern to a project-wide recommendation for CQRS read models (Queries). This pattern bypasses the Domain entity and maps raw DB query results directly to an Application DTO (Record) to avoid unnecessary overhead (N+1 queries, excess memory allocation, unused columns) when retrieving lists or read-only reports.

## Scope
* **MADR Document:** Create `docs/decisions/0024-cqrs-query-direct-projection-strategy.md` to formally record this architectural decision.
* **Example Implementation:** Add a practical code example to the `boilerplate` module to demonstrate how an Outbound Adapter directly queries the DB and returns a DTO for a Query Port.

## Architecture & Implementation Strategy

1. **MADR Creation (0024):**
   * **Context:** Hexagonal Architecture strictly separates domain from persistence. Mapping DB Entities -> Domain Entities -> DTOs is heavy for simple reads.
   * **Decision:** For Commands (state changes), use the strict `DB -> Adapter -> MapStruct -> Domain` path to enforce business rules. For Queries (read-only), allow the Outbound Adapter to execute optimized SQL (via jOOQ or JdbcTemplate) and project the result directly into an Application layer DTO (`Record`).
   * **Consequences:** Greatly improved read performance and reduced boilerplate for queries, at the cost of slightly divergent code paths between reads and writes.

2. **Example Component Generation:**
   * **Application Layer (`boilerplate-application`):**
     * Create `io.github.ppzxc.boilerplate.application.dto.TodoSummary` (Record) representing the read model.
     * Create `io.github.ppzxc.boilerplate.application.port.input.query.FindTodoSummariesQuery`.
     * Create `io.github.ppzxc.boilerplate.application.port.output.query.LoadTodoSummariesPort`.
   * **Adapter Output (`boilerplate-adapter-output-persist`):**
     * Create `TodoSummaryDirectProjectionAdapter` implementing `LoadTodoSummariesPort`.
     * Use `JdbcTemplate` or `jOOQ` to `SELECT id, title FROM todo` and map directly to `TodoSummary` (bypassing MapStruct and `Todo` domain entity).
   * **Adapter Input (`boilerplate-adapter-input-api`):**
     * Add an endpoint `GET /api/v1/todos/summaries` calling the Query port.

## Testing & Verification
* Run `./gradlew test` to ensure ArchUnit rules still pass (ArchUnit does not forbid Outbound Adapters from returning Application DTOs).
* The MADR serves as the formal documentation for future developers.
