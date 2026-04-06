# Transaction Rollback Integration Test Design

## Objective
Add a dedicated `@SpringBootTest` to the `boilerplate-boot-api` module that definitively proves that the AutoConfiguration-based `@Transactional` decorators (as defined in ADR-0012) correctly roll back multiple distinct database inserts if an exception is thrown mid-execution. This provides a visible safeguard and educational example for the development team.

## Scope
* **Location:** `boilerplate-boot-api/src/test/java/io/github/ppzxc/boilerplate/`
* **Artifacts Created:**
  * `TransactionRollbackIT.java` (Integration test file)
  * Dummy components specifically for this test (DummyUseCase, DummyService, DummyPort, DummyAdapter) inside the test context.
* **Documentation Update:** Update `docs/decisions/0012-transaction-management-strategy.md` or a new README section to point to this test as the source of truth for transaction boundaries.

## Architecture & Implementation Strategy

1. **Dummy Test Components:**
   * **DummyDomain:** A simple record containing an ID and a Name.
   * **SaveDummyPort:** An interface to save the dummy domain.
   * **TriggerExceptionUseCase:** An interface with a `execute()` method.
   * **TriggerExceptionService:** A service implementing the UseCase that takes the `SaveDummyPort`. Inside `execute()`, it will:
     1. Call `saveDummyPort.save(dummy1)`.
     2. Throw a `RuntimeException("Intentional rollback exception")`.
   * **DummyPersistAdapter:** An adapter implementing `SaveDummyPort` that uses jOOQ or Spring Data JPA (depending on the module's actual setup) to insert into a real/testcontainers DB. Since we are testing the application context's transaction proxy, we can use the existing `TodoJooqRepository` or a dummy entity.

2. **AutoConfiguration Test Configuration:**
   * Register the `TriggerExceptionService` as a Bean within the test.
   * The `boilerplate-application-autoconfiguration` logic should automatically wrap this Bean in a `@Transactional` proxy (assuming it follows the established rules, or we manually wrap it if the test context requires it).
   * Actually, the `application-autoconfiguration` module automatically proxies any Bean ending with `Service` or implementing a `UseCase`.

3. **Test Execution (`TransactionRollbackIT`):**
   * Pre-condition: Check current row count in the table (e.g., 0).
   * Action: Catch the exception thrown by `triggerExceptionUseCase.execute()`.
   * Post-condition: Verify the row count remains 0. Verify that `dummy1` was *not* persisted, proving the transaction rolled back successfully.

## Error Handling & Edge Cases
* Ensure the Testcontainers environment properly initializes the DB schema for the dummy table or existing table.
* Ensure the `RuntimeException` is not swallowed by the controller advice if tested via API (we will test the UseCase directly to bypass the web layer and isolate the transaction proxy).

## Testing & Verification
* Run `./gradlew :boilerplate-boot-api:test --tests "*TransactionRollbackIT*"`
* Test MUST pass, confirming the rollback behavior.
