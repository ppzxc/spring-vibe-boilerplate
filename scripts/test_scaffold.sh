#!/bin/bash
set -e

# Run the scaffold script
./scripts/scaffold.sh TestDomain

# Verify files exist
FILES=(
  "boilerplate/boilerplate-domain/src/main/java/io/github/ppzxc/boilerplate/domain/TestDomain.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/input/command/CreateTestDomainUseCase.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/input/query/FindTestDomainQuery.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/output/command/SaveTestDomainPort.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/output/query/LoadTestDomainPort.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/service/command/CreateTestDomainService.java"
  "boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/service/query/FindTestDomainService.java"
  "boilerplate/boilerplate-adapter-input-api/src/main/java/io/github/ppzxc/boilerplate/adapter/input/api/TestDomainController.java"
  "boilerplate/boilerplate-adapter-input-api/src/main/java/io/github/ppzxc/boilerplate/adapter/input/api/TestDomainRequest.java"
  "boilerplate/boilerplate-adapter-input-api/src/main/java/io/github/ppzxc/boilerplate/adapter/input/api/TestDomainResponse.java"
  "boilerplate/boilerplate-adapter-input-api/src/main/java/io/github/ppzxc/boilerplate/adapter/input/api/TestDomainApiMapper.java"
  "boilerplate/boilerplate-adapter-output-persist/src/main/java/io/github/ppzxc/boilerplate/adapter/output/persist/TestDomainPersistAdapter.java"
  "boilerplate/boilerplate-adapter-output-persist/src/main/java/io/github/ppzxc/boilerplate/adapter/output/persist/TestDomainJooqRepository.java"
  "boilerplate/boilerplate-adapter-output-persist/src/main/java/io/github/ppzxc/boilerplate/adapter/output/persist/TestDomainPersistMapper.java"
)

for file in "${FILES[@]}"; do
  if [ ! -f "$file" ]; then
    echo "FAIL: Missing file $file"
    exit 1
  fi
done

# Verify Flyway migration
MIGRATION_FILE=$(ls boilerplate/boilerplate-adapter-output-persist/src/main/resources/db/migration/*__create_testdomain_table.sql 2>/dev/null | head -n 1)
if [ -z "$MIGRATION_FILE" ]; then
  echo "FAIL: Missing Flyway migration file"
  exit 1
fi

echo "PASS: All files generated successfully."

# Cleanup test files
rm -rf boilerplate/boilerplate-domain/src/main/java/io/github/ppzxc/boilerplate/domain/TestDomain.java
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/input/command/*TestDomain*
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/input/query/*TestDomain*
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/output/command/*TestDomain*
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/port/output/query/*TestDomain*
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/service/command/*TestDomain*
rm -rf boilerplate/boilerplate-application/src/main/java/io/github/ppzxc/boilerplate/application/service/query/*TestDomain*
rm -rf boilerplate/boilerplate-adapter-input-api/src/main/java/io/github/ppzxc/boilerplate/adapter/input/api/*TestDomain*
rm -rf boilerplate/boilerplate-adapter-output-persist/src/main/java/io/github/ppzxc/boilerplate/adapter/output/persist/*TestDomain*
rm -rf boilerplate/boilerplate-adapter-output-persist/src/main/resources/db/migration/*__create_testdomain_table.sql