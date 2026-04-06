#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <DomainName>"
  echo "Example: $0 Order"
  exit 1
fi

DOMAIN=$1
# Lowercase domain for packages and DB tables
DOMAIN_LOWER=$(echo "$DOMAIN" | tr '[:upper:]' '[:lower:]')
# Camel case for variable names
DOMAIN_CAMEL="$(tr '[:upper:]' '[:lower:]' <<< ${DOMAIN:0:1})${DOMAIN:1}"

PKG_BASE="io/github/ppzxc/boilerplate"
PKG_BASE_DOT="io.github.ppzxc.boilerplate"

# Module paths
DOMAIN_MOD="boilerplate/boilerplate-domain/src/main/java/$PKG_BASE"
APP_MOD="boilerplate/boilerplate-application/src/main/java/$PKG_BASE"
API_MOD="boilerplate/boilerplate-adapter-input-api/src/main/java/$PKG_BASE"
PERSIST_MOD="boilerplate/boilerplate-adapter-output-persist/src/main/java/$PKG_BASE"
MIGRATION_DIR="boilerplate/boilerplate-adapter-output-persist/src/main/resources/db/migration"

echo "Generating files for Domain: $DOMAIN..."

# 1. Domain
mkdir -p "$DOMAIN_MOD/domain"
cat <<EOF > "$DOMAIN_MOD/domain/$DOMAIN.java"
package $PKG_BASE_DOT.domain;

public record $DOMAIN() {
}
EOF

# 2. Application Ports
mkdir -p "$APP_MOD/application/port/input/command"
mkdir -p "$APP_MOD/application/port/input/query"
mkdir -p "$APP_MOD/application/port/output/command"
mkdir -p "$APP_MOD/application/port/output/query"

cat <<EOF > "$APP_MOD/application/port/input/command/Create${DOMAIN}UseCase.java"
package $PKG_BASE_DOT.application.port.input.command;

public interface Create${DOMAIN}UseCase {
    void create$DOMAIN();
}
EOF

cat <<EOF > "$APP_MOD/application/port/input/query/Find${DOMAIN}Query.java"
package $PKG_BASE_DOT.application.port.input.query;

public interface Find${DOMAIN}Query {
    $PKG_BASE_DOT.domain.$DOMAIN find$DOMAIN();
}
EOF

cat <<EOF > "$APP_MOD/application/port/output/command/Save${DOMAIN}Port.java"
package $PKG_BASE_DOT.application.port.output.command;

public interface Save${DOMAIN}Port {
    void save($PKG_BASE_DOT.domain.$DOMAIN $DOMAIN_CAMEL);
}
EOF

cat <<EOF > "$APP_MOD/application/port/output/query/Load${DOMAIN}Port.java"
package $PKG_BASE_DOT.application.port.output.query;

public interface Load${DOMAIN}Port {
    $PKG_BASE_DOT.domain.$DOMAIN load();
}
EOF

# 3. Application Services
mkdir -p "$APP_MOD/application/service/command"
mkdir -p "$APP_MOD/application/service/query"

cat <<EOF > "$APP_MOD/application/service/command/Create${DOMAIN}Service.java"
package $PKG_BASE_DOT.application.service.command;

import $PKG_BASE_DOT.application.port.input.command.Create${DOMAIN}UseCase;
import $PKG_BASE_DOT.application.port.output.command.Save${DOMAIN}Port;

public class Create${DOMAIN}Service implements Create${DOMAIN}UseCase {
    private final Save${DOMAIN}Port save${DOMAIN}Port;

    public Create${DOMAIN}Service(Save${DOMAIN}Port save${DOMAIN}Port) {
        this.save${DOMAIN}Port = save${DOMAIN}Port;
    }

    @Override
    public void create$DOMAIN() {
        // Implement logic
    }
}
EOF

cat <<EOF > "$APP_MOD/application/service/query/Find${DOMAIN}Service.java"
package $PKG_BASE_DOT.application.service.query;

import $PKG_BASE_DOT.application.port.input.query.Find${DOMAIN}Query;
import $PKG_BASE_DOT.application.port.output.query.Load${DOMAIN}Port;
import $PKG_BASE_DOT.domain.$DOMAIN;

public class Find${DOMAIN}Service implements Find${DOMAIN}Query {
    private final Load${DOMAIN}Port load${DOMAIN}Port;

    public Find${DOMAIN}Service(Load${DOMAIN}Port load${DOMAIN}Port) {
        this.load${DOMAIN}Port = load${DOMAIN}Port;
    }

    @Override
    public $DOMAIN find$DOMAIN() {
        return load${DOMAIN}Port.load();
    }
}
EOF

# 4. Adapter Input API
mkdir -p "$API_MOD/adapter/input/api"

cat <<EOF > "$API_MOD/adapter/input/api/${DOMAIN}Controller.java"
package $PKG_BASE_DOT.adapter.input.api;

import org.springframework.web.bind.annotation.RestController;
import $PKG_BASE_DOT.application.port.input.command.Create${DOMAIN}UseCase;
import $PKG_BASE_DOT.application.port.input.query.Find${DOMAIN}Query;

@RestController
public class ${DOMAIN}Controller {
    private final Create${DOMAIN}UseCase create${DOMAIN}UseCase;
    private final Find${DOMAIN}Query find${DOMAIN}Query;

    public ${DOMAIN}Controller(Create${DOMAIN}UseCase create${DOMAIN}UseCase, Find${DOMAIN}Query find${DOMAIN}Query) {
        this.create${DOMAIN}UseCase = create${DOMAIN}UseCase;
        this.find${DOMAIN}Query = find${DOMAIN}Query;
    }
}
EOF

cat <<EOF > "$API_MOD/adapter/input/api/${DOMAIN}Request.java"
package $PKG_BASE_DOT.adapter.input.api;

public record ${DOMAIN}Request() {}
EOF

cat <<EOF > "$API_MOD/adapter/input/api/${DOMAIN}Response.java"
package $PKG_BASE_DOT.adapter.input.api;

public record ${DOMAIN}Response() {}
EOF

cat <<EOF > "$API_MOD/adapter/input/api/${DOMAIN}ApiMapper.java"
package $PKG_BASE_DOT.adapter.input.api;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ${DOMAIN}ApiMapper {
}
EOF

# 5. Adapter Output Persist
mkdir -p "$PERSIST_MOD/adapter/output/persist"

cat <<EOF > "$PERSIST_MOD/adapter/output/persist/${DOMAIN}PersistAdapter.java"
package $PKG_BASE_DOT.adapter.output.persist;

import org.springframework.stereotype.Component;
import $PKG_BASE_DOT.application.port.output.command.Save${DOMAIN}Port;
import $PKG_BASE_DOT.application.port.output.query.Load${DOMAIN}Port;
import $PKG_BASE_DOT.domain.$DOMAIN;

@Component
public class ${DOMAIN}PersistAdapter implements Save${DOMAIN}Port, Load${DOMAIN}Port {
    private final ${DOMAIN}JooqRepository repository;
    private final ${DOMAIN}PersistMapper mapper;

    public ${DOMAIN}PersistAdapter(${DOMAIN}JooqRepository repository, ${DOMAIN}PersistMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save($DOMAIN $DOMAIN_CAMEL) {
        // repository.save(...);
    }

    @Override
    public $DOMAIN load() {
        return null;
    }
}
EOF

cat <<EOF > "$PERSIST_MOD/adapter/output/persist/${DOMAIN}JooqRepository.java"
package $PKG_BASE_DOT.adapter.output.persist;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class ${DOMAIN}JooqRepository {
    private final DSLContext dsl;

    public ${DOMAIN}JooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }
}
EOF

cat <<EOF > "$PERSIST_MOD/adapter/output/persist/${DOMAIN}PersistMapper.java"
package $PKG_BASE_DOT.adapter.output.persist;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ${DOMAIN}PersistMapper {
}
EOF

# 6. Flyway Migration
mkdir -p "$MIGRATION_DIR"
TIMESTAMP=$(date +%Y%m%d%H%M%S)
MIGRATION_FILE="$MIGRATION_DIR/V${TIMESTAMP}__create_${DOMAIN_LOWER}_table.sql"

cat <<EOF > "$MIGRATION_FILE"
CREATE TABLE ${DOMAIN_LOWER} (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
EOF

echo "Done! Scaffolding for $DOMAIN completed."
echo "Run './gradlew spotlessApply' and './gradlew compileJava' after filling out the Flyway migration."