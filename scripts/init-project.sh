#!/usr/bin/env bash
# init-project.sh — 보일러플레이트를 새 프로젝트로 초기화한다.
#
# 사용법:
#   ./scripts/init-project.sh <new-group-id> <new-artifact-name> [--dry-run]
#
# 예시:
#   ./scripts/init-project.sh com.example myapp
#   ./scripts/init-project.sh com.example myapp --dry-run
#
# 변경 내용:
#   - 패키지명: io.github.ppzxc.boilerplate → <new-group-id>.<new-artifact-name>
#   - 모듈명: boilerplate-* → <new-artifact-name>-*
#   - 루트 프로젝트명: boilerplate → <new-artifact-name>
#   - 디렉토리명: boilerplate/ → <new-artifact-name>/

set -euo pipefail

# ── 인자 파싱 ─────────────────────────────────────────────────────────────────

usage() {
  echo "Usage: $0 <new-group-id> <new-artifact-name> [--dry-run]" >&2
  echo "  new-group-id:      e.g. com.example" >&2
  echo "  new-artifact-name: e.g. myapp (alphanumeric and hyphens only)" >&2
  exit 1
}

if [[ $# -lt 2 ]]; then usage; fi

NEW_GROUP="$1"
NEW_ARTIFACT="$2"
DRY_RUN=false

for arg in "${@:3}"; do
  if [[ "$arg" == "--dry-run" ]]; then
    DRY_RUN=true
  fi
done

# ── 입력 검증 ─────────────────────────────────────────────────────────────────

if [[ ! "$NEW_GROUP" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*$ ]]; then
  echo "Error: new-group-id must be a valid reverse-domain identifier (e.g. com.example)" >&2
  exit 1
fi

if [[ ! "$NEW_ARTIFACT" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "Error: new-artifact-name must be lowercase alphanumeric with hyphens (e.g. myapp)" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# ── Git dirty check ────────────────────────────────────────────────────────────

cd "$PROJECT_ROOT"

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Error: Working directory has uncommitted changes. Please commit or stash first." >&2
  exit 1
fi

# ── 치환 값 계산 ──────────────────────────────────────────────────────────────

OLD_GROUP="io.github.ppzxc.boilerplate"
OLD_GROUP_PATH="io/github/ppzxc/boilerplate"
OLD_ARTIFACT="boilerplate"

NEW_GROUP_PATH="${NEW_GROUP//./\/}/${NEW_ARTIFACT}"
NEW_PACKAGE="${NEW_GROUP}.${NEW_ARTIFACT}"

echo "=== init-project ==="
echo "  old package : ${OLD_GROUP}"
echo "  new package : ${NEW_PACKAGE}"
echo "  old artifact: ${OLD_ARTIFACT}"
echo "  new artifact: ${NEW_ARTIFACT}"
if [[ "$DRY_RUN" == "true" ]]; then
  echo "  mode        : DRY-RUN (no changes will be made)"
else
  echo "  mode        : APPLY"
fi
echo ""

# ── 헬퍼 함수 ────────────────────────────────────────────────────────────────

run() {
  if [[ "$DRY_RUN" == "true" ]]; then
    echo "[DRY-RUN] $*"
  else
    eval "$@"
  fi
}

# 텍스트 파일 내용 치환 (macOS/Linux 모두 지원)
replace_in_file() {
  local file="$1"
  local old="$2"
  local new="$3"
  if grep -qF "$old" "$file" 2>/dev/null; then
    if [[ "$DRY_RUN" == "true" ]]; then
      echo "[DRY-RUN] sed: $file : '$old' → '$new'"
    else
      sed -i "s|${old}|${new}|g" "$file"
    fi
  fi
}

# ── 1단계: 텍스트 치환 (파일 내용) ──────────────────────────────────────────

echo "--- Step 1: Replace text in files ---"

# 대상 파일: *.java, *.kts, *.yml, *.yaml, *.md, *.properties, *.xml, *.toml
mapfile -t TEXT_FILES < <(
  find "$PROJECT_ROOT" \
    -not -path "*/\.*" \
    -not -path "*/build/*" \
    -not -path "*/generated*" \
    -not -path "*/.worktrees/*" \
    -type f \
    \( -name "*.java" -o -name "*.kts" -o -name "*.yml" -o -name "*.yaml" \
       -o -name "*.md" -o -name "*.properties" -o -name "*.xml" -o -name "*.toml" \
       -o -name "*.imports" -o -name "*.sh" \) 2>/dev/null
)

for file in "${TEXT_FILES[@]}"; do
  # 패키지 경로 (디렉토리 구분자 포함)
  replace_in_file "$file" "${OLD_GROUP_PATH}" "${NEW_GROUP_PATH}"
  # 패키지명
  replace_in_file "$file" "${OLD_GROUP}" "${NEW_PACKAGE}"
  # 모듈명 (boilerplate- 접두사)
  replace_in_file "$file" "${OLD_ARTIFACT}-" "${NEW_ARTIFACT}-"
  # 프로젝트 루트명 단독
  replace_in_file "$file" "\"${OLD_ARTIFACT}\"" "\"${NEW_ARTIFACT}\""
  # application.yml의 app.name 등에서 단독 사용
  replace_in_file "$file" ": ${OLD_ARTIFACT}" ": ${NEW_ARTIFACT}"
  replace_in_file "$file" "=${OLD_ARTIFACT}" "=${NEW_ARTIFACT}"
done

# ── 2단계: Java 패키지 디렉토리 이름 변경 ────────────────────────────────────

echo "--- Step 2: Rename Java package directories ---"

OLD_JAVA_BASE="src/main/java/${OLD_GROUP_PATH}"
NEW_JAVA_BASE="src/main/java/${NEW_GROUP_PATH}"

OLD_TEST_BASE="src/test/java/${OLD_GROUP_PATH}"
NEW_TEST_BASE="src/test/java/${NEW_GROUP_PATH}"

OLD_FIXTURE_BASE="src/testFixtures/java/${OLD_GROUP_PATH}"
NEW_FIXTURE_BASE="src/testFixtures/java/${NEW_GROUP_PATH}"

# 각 모듈의 패키지 디렉토리를 재귀적으로 이동
for module_dir in "$PROJECT_ROOT/${OLD_ARTIFACT}/"*/; do
  for src_pair in \
    "${OLD_JAVA_BASE}:${NEW_JAVA_BASE}" \
    "${OLD_TEST_BASE}:${NEW_TEST_BASE}" \
    "${OLD_FIXTURE_BASE}:${NEW_FIXTURE_BASE}"; do

    old_rel="${src_pair%%:*}"
    new_rel="${src_pair##*:}"
    old_path="${module_dir}${old_rel}"
    new_path="${module_dir}${new_rel}"

    if [[ -d "$old_path" ]]; then
      run "mkdir -p \"$(dirname "$new_path")\""
      run "mv \"$old_path\" \"$new_path\""
      # 빈 부모 디렉토리 정리
      run "find \"$(dirname "$old_path")\" -type d -empty -delete 2>/dev/null || true"
    fi
  done
done

# ── 3단계: 모듈 디렉토리 이름 변경 ──────────────────────────────────────────

echo "--- Step 3: Rename module directories ---"

if [[ -d "$PROJECT_ROOT/${OLD_ARTIFACT}" ]]; then
  run "mv \"$PROJECT_ROOT/${OLD_ARTIFACT}\" \"$PROJECT_ROOT/${NEW_ARTIFACT}\""
fi

# ── 4단계: CLAUDE.md 베이스 패키지 업데이트 ─────────────────────────────────

echo "--- Step 4: Update CLAUDE.md ---"
replace_in_file "$PROJECT_ROOT/.claude/CLAUDE.md" "${OLD_GROUP}" "${NEW_PACKAGE}"

# ── 완료 ─────────────────────────────────────────────────────────────────────

echo ""
if [[ "$DRY_RUN" == "true" ]]; then
  echo "=== DRY-RUN complete. No files were modified. ==="
  echo "    Run without --dry-run to apply changes."
else
  echo "=== Done! Next steps ==="
  echo "    1. ./gradlew compileJava       # 컴파일 확인"
  echo "    2. ./gradlew spotlessApply     # 포맷 수정"
  echo "    3. ./gradlew test              # 테스트 확인"
  echo "    4. git add -A && git commit -m 'chore: rename project to ${NEW_ARTIFACT}'"
fi
