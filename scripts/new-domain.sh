#!/usr/bin/env bash
# new-domain.sh — 새 도메인을 전 레이어에 걸쳐 스캐폴딩한다.
#
# 사용법:
#   ./scripts/new-domain.sh <DomainName>
#
# 예시:
#   ./scripts/new-domain.sh Order
#   ./scripts/new-domain.sh Product
#
# 생성되는 파일:
#   Domain:
#     domain/<DomainName>.java
#   Application Ports:
#     application/port/input/command/Create<DomainName>UseCase.java
#     application/port/input/command/Delete<DomainName>UseCase.java
#     application/port/input/query/Find<DomainName>Query.java
#     application/port/output/command/Save<DomainName>Port.java
#     application/port/output/command/Delete<DomainName>Port.java
#     application/port/output/query/Find<DomainName>Port.java
#   Application Services:
#     application/service/command/Create<DomainName>Service.java
#     application/service/command/Delete<DomainName>Service.java
#     application/service/query/Find<DomainName>Service.java
#   Tests:
#     application/.../Create<DomainName>ServiceTest.java
#     application/.../Find<DomainName>ServiceTest.java

set -euo pipefail

# ── 인자 파싱 ─────────────────────────────────────────────────────────────────

usage() {
  echo "Usage: $0 <DomainName>" >&2
  echo "  DomainName: PascalCase (e.g. Order, Product, BlogPost)" >&2
  exit 1
}

if [[ $# -ne 1 ]]; then usage; fi

DOMAIN="$1"

if [[ ! "$DOMAIN" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
  echo "Error: DomainName must be PascalCase (e.g. Order, BlogPost)" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# ── 프로젝트 메타 정보 추출 ───────────────────────────────────────────────────

# settings.gradle.kts 에서 루트 프로젝트명 추출
ARTIFACT=$(grep 'rootProject.name' "$PROJECT_ROOT/settings.gradle.kts" \
  | sed 's/.*= *"\(.*\)".*/\1/')

# 기존 domain 패키지에서 basePackage 추출
DOMAIN_JAVA_DIRS=$(find "$PROJECT_ROOT/${ARTIFACT}/${ARTIFACT}-domain/src/main/java" \
  -type d -name "domain" 2>/dev/null | head -1)

if [[ -z "$DOMAIN_JAVA_DIRS" ]]; then
  echo "Error: Cannot find domain directory. Is the project structure correct?" >&2
  exit 1
fi

BASE_PKG=$(echo "$DOMAIN_JAVA_DIRS" \
  | sed "s|.*/src/main/java/||" \
  | sed 's|/domain$||' \
  | tr '/' '.')

echo "=== new-domain ==="
echo "  artifact   : ${ARTIFACT}"
echo "  base-pkg   : ${BASE_PKG}"
echo "  domain     : ${DOMAIN}"
echo ""

# ── 파생 값 계산 ──────────────────────────────────────────────────────────────

DOMAIN_LOWER="${DOMAIN,}"  # camelCase first letter

PKG_DOMAIN="${BASE_PKG}.domain"
PKG_APP="${BASE_PKG}.application"
PKG_CMD_PORT="${PKG_APP}.port.input.command"
PKG_QUERY_PORT="${PKG_APP}.port.input.query"
PKG_OUT_CMD="${PKG_APP}.port.output.command"
PKG_OUT_QUERY="${PKG_APP}.port.output.query"
PKG_SVC_CMD="${PKG_APP}.service.command"
PKG_SVC_QUERY="${PKG_APP}.service.query"

MODULE_DOMAIN="${ARTIFACT}/${ARTIFACT}-domain"
MODULE_APP="${ARTIFACT}/${ARTIFACT}-application"

to_path() { echo "$1" | tr '.' '/'; }

JAVA_DOMAIN="${PROJECT_ROOT}/${MODULE_DOMAIN}/src/main/java/$(to_path "$PKG_DOMAIN")"
JAVA_APP="${PROJECT_ROOT}/${MODULE_APP}/src/main/java/$(to_path "$PKG_APP")"
JAVA_TEST="${PROJECT_ROOT}/${MODULE_APP}/src/test/java/$(to_path "$PKG_APP")"

# ── 파일 생성 헬퍼 ───────────────────────────────────────────────────────────

write_file() {
  local path="$1"
  local content="$2"
  if [[ -f "$path" ]]; then
    echo "  [SKIP] already exists: $path"
    return
  fi
  mkdir -p "$(dirname "$path")"
  echo "$content" > "$path"
  echo "  [NEW]  $path"
}

# ── 1. Domain Entity ─────────────────────────────────────────────────────────

echo "--- Generating domain ---"

write_file "${JAVA_DOMAIN}/${DOMAIN}.java" \
"package ${PKG_DOMAIN};

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ${DOMAIN} {

  private @Nullable Long id;
  private String name;

  public static ${DOMAIN} create(String name) {
    if (name == null || name.isBlank()) {
      throw new DomainException(ErrorCode.INVALID_ARGUMENT, \"Name must not be blank\");
    }
    return new ${DOMAIN}(null, name.strip());
  }

  public static ${DOMAIN} reconstitute(Long id, String name) {
    return new ${DOMAIN}(id, name);
  }
}"

# ── 2. Inbound Command Ports ──────────────────────────────────────────────────

echo "--- Generating inbound ports ---"

write_file "${JAVA_APP}/port/input/command/Create${DOMAIN}UseCase.java" \
"package ${PKG_CMD_PORT};

import ${PKG_DOMAIN}.${DOMAIN};

public interface Create${DOMAIN}UseCase {

  ${DOMAIN} create(String name);
}"

write_file "${JAVA_APP}/port/input/command/Delete${DOMAIN}UseCase.java" \
"package ${PKG_CMD_PORT};

public interface Delete${DOMAIN}UseCase {

  void delete(Long id);
}"

# ── 3. Inbound Query Ports ────────────────────────────────────────────────────

write_file "${JAVA_APP}/port/input/query/Find${DOMAIN}Query.java" \
"package ${PKG_QUERY_PORT};

import ${PKG_DOMAIN}.${DOMAIN};

public interface Find${DOMAIN}Query {

  ${DOMAIN} findById(Long id);
}"

# ── 4. Outbound Command Ports ─────────────────────────────────────────────────

echo "--- Generating outbound ports ---"

write_file "${JAVA_APP}/port/output/command/Save${DOMAIN}Port.java" \
"package ${PKG_OUT_CMD};

import ${PKG_DOMAIN}.${DOMAIN};

public interface Save${DOMAIN}Port {

  ${DOMAIN} save(${DOMAIN} ${DOMAIN_LOWER});
}"

write_file "${JAVA_APP}/port/output/command/Delete${DOMAIN}Port.java" \
"package ${PKG_OUT_CMD};

public interface Delete${DOMAIN}Port {

  void deleteById(Long id);
}"

# ── 5. Outbound Query Ports ───────────────────────────────────────────────────

write_file "${JAVA_APP}/port/output/query/Find${DOMAIN}Port.java" \
"package ${PKG_OUT_QUERY};

import ${PKG_DOMAIN}.${DOMAIN};
import java.util.Optional;

public interface Find${DOMAIN}Port {

  Optional<${DOMAIN}> findById(Long id);
}"

# ── 6. Application Services ───────────────────────────────────────────────────

echo "--- Generating application services ---"

write_file "${JAVA_APP}/service/command/Create${DOMAIN}Service.java" \
"package ${PKG_SVC_CMD};

import ${PKG_CMD_PORT}.Create${DOMAIN}UseCase;
import ${PKG_DOMAIN}.${DOMAIN};
import ${PKG_OUT_CMD}.Save${DOMAIN}Port;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Create${DOMAIN}Service implements Create${DOMAIN}UseCase {

  private final Save${DOMAIN}Port save${DOMAIN}Port;

  @Override
  public ${DOMAIN} create(String name) {
    ${DOMAIN} ${DOMAIN_LOWER} = ${DOMAIN}.create(name);
    return save${DOMAIN}Port.save(${DOMAIN_LOWER});
  }
}"

write_file "${JAVA_APP}/service/command/Delete${DOMAIN}Service.java" \
"package ${PKG_SVC_CMD};

import ${PKG_CMD_PORT}.Delete${DOMAIN}UseCase;
import ${PKG_OUT_CMD}.Delete${DOMAIN}Port;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Delete${DOMAIN}Service implements Delete${DOMAIN}UseCase {

  private final Delete${DOMAIN}Port delete${DOMAIN}Port;

  @Override
  public void delete(Long id) {
    delete${DOMAIN}Port.deleteById(id);
  }
}"

write_file "${JAVA_APP}/service/query/Find${DOMAIN}Service.java" \
"package ${PKG_SVC_QUERY};

import ${PKG_QUERY_PORT}.Find${DOMAIN}Query;
import ${PKG_DOMAIN}.${DOMAIN};
import ${PKG_DOMAIN}.DomainException;
import ${PKG_DOMAIN}.ErrorCode;
import ${PKG_OUT_QUERY}.Find${DOMAIN}Port;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Find${DOMAIN}Service implements Find${DOMAIN}Query {

  private final Find${DOMAIN}Port find${DOMAIN}Port;

  @Override
  public ${DOMAIN} findById(Long id) {
    return find${DOMAIN}Port.findById(id)
        .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, \"${DOMAIN} not found: \" + id));
  }
}"

# ── 7. Service Tests ──────────────────────────────────────────────────────────

echo "--- Generating service tests ---"

write_file "${JAVA_TEST}/service/command/Create${DOMAIN}ServiceTest.java" \
"package ${PKG_SVC_CMD};

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ${PKG_DOMAIN}.${DOMAIN};
import ${PKG_OUT_CMD}.Save${DOMAIN}Port;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Create${DOMAIN}ServiceTest {

  @Mock Save${DOMAIN}Port save${DOMAIN}Port;
  @InjectMocks Create${DOMAIN}Service sut;

  @Test
  void create_saves_and_returns_domain() {
    ${DOMAIN} saved = ${DOMAIN}.reconstitute(1L, \"sample\");
    when(save${DOMAIN}Port.save(any())).thenReturn(saved);

    ${DOMAIN} result = sut.create(\"sample\");

    assertThat(result.getName()).isEqualTo(\"sample\");
  }
}"

write_file "${JAVA_TEST}/service/query/Find${DOMAIN}ServiceTest.java" \
"package ${PKG_SVC_QUERY};

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ${PKG_DOMAIN}.${DOMAIN};
import ${PKG_DOMAIN}.DomainException;
import ${PKG_OUT_QUERY}.Find${DOMAIN}Port;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Find${DOMAIN}ServiceTest {

  @Mock Find${DOMAIN}Port find${DOMAIN}Port;
  @InjectMocks Find${DOMAIN}Service sut;

  @Test
  void findById_returns_domain_when_found() {
    ${DOMAIN} domain = ${DOMAIN}.reconstitute(1L, \"sample\");
    when(find${DOMAIN}Port.findById(1L)).thenReturn(Optional.of(domain));

    ${DOMAIN} result = sut.findById(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void findById_throws_when_not_found() {
    when(find${DOMAIN}Port.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> sut.findById(99L))
        .isInstanceOf(DomainException.class);
  }
}"

# ── 완료 ─────────────────────────────────────────────────────────────────────

echo ""
echo "=== Done! Next steps ==="
echo "  1. Implement outbound adapter (persist/cache) for ${DOMAIN}"
echo "  2. Register Beans in ApplicationAutoConfiguration:"
echo "       Create${DOMAIN}UseCase, Delete${DOMAIN}UseCase, Find${DOMAIN}Query"
echo "  3. Add REST Controller in adapter-input-api (optional)"
echo "  4. ./gradlew spotlessApply && ./gradlew compileJava"
