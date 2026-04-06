# ArchUnit Rules Enhancement Design

## Objective
Enforce the dependency rules defined in ADR-0001 (Hexagonal Architecture) and ADR-0013 (Object Mapping Strategy) strictly within the adapter layers. The current test suite only guards the domain and application layers against Spring/JPA intrusion. This design adds ArchUnit tests to the adapter layers to prevent bypasses of the application layer and direct adapter-to-adapter coupling.

## Scope
* **New Tests Created:**
  * `AdapterInputArchitectureTest.java` (in `boilerplate-adapter-input-api`)
  * `AdapterOutputArchitectureTest.java` (in `boilerplate-adapter-output-persist` and potentially `boilerplate-adapter-output-cache`)
* **Existing Tests Updated:**
  * `DomainArchitectureTest.java` / `ApplicationArchitectureTest.java` (if Mapper boundary rules are not already enforced).

## Architecture Rules to Implement

1. **Inbound Adapter Constraints (`AdapterInputArchitectureTest`):**
   * REST Controllers and WS Handlers must **ONLY** depend on `io.github.ppzxc.boilerplate.application.port.input..*` (UseCases, Queries) or `io.github.ppzxc.boilerplate.domain..*`.
   * They must **NOT** depend on `io.github.ppzxc.boilerplate.application.port.output..*`.
   * They must **NOT** depend on other adapter modules (e.g., `boilerplate.adapter.output..*`).

2. **Outbound Adapter Constraints (`AdapterOutputArchitectureTest`):**
   * Persist and Cache Adapters must **ONLY** implement `io.github.ppzxc.boilerplate.application.port.output..*`.
   * They must **NOT** depend on or call `io.github.ppzxc.boilerplate.application.port.input..*` (UseCases, Queries).
   * They must **NOT** depend on other adapter modules.

3. **Mapper Locality Constraints (ADR-0013):**
   * MapStruct interfaces (`*Mapper`) and their generated implementations MUST NOT be used by or referenced from `boilerplate-domain` or `boilerplate-application`.
   * MapStruct generated implementations (`*MapperImpl`) should be ignored by the ArchUnit tests if they trigger false positives (already partially handled by Checkstyle suppressions, need to ensure ArchUnit ignores `.generated.` packages or `Impl` suffixes).

## Error Handling & Edge Cases
* Some existing adapter classes might violate these new strict rules. If so, they must be fixed during implementation.
* ArchUnit `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS` must be used to prevent these rules from inadvertently failing test code itself.

## Testing & Verification
* Run `./gradlew test` to ensure the new ArchUnit tests correctly execute and pass against the current boilerplate codebase.
