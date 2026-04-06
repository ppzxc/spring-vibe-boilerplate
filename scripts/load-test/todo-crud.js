/**
 * Todo CRUD 부하 테스트 — k6
 *
 * 사용법:
 *   k6 run scripts/load-test/todo-crud.js
 *   k6 run --vus 50 --duration 60s scripts/load-test/todo-crud.js
 *   BASE_URL=http://staging:8080 k6 run scripts/load-test/todo-crud.js
 *
 * 시나리오:
 *   1. Todo 생성 (POST /api/todos)
 *   2. 생성된 Todo 조회 (GET /api/todos/{id})
 *   3. Todo 목록 조회 (GET /api/todos)
 *   4. Todo 완료 처리 (PUT /api/todos/{id})
 *   5. Todo 삭제 (DELETE /api/todos/{id})
 *
 * 결과 해석:
 *   - http_req_duration p(95) < 200ms → 양호
 *   - http_req_failed < 1% → 양호
 *   - hikaricp_connections_pending > 0 지속 → pool size 부족
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

// ── 설정 ────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  scenarios: {
    // 점진적 증가 (워밍업 → 정상 부하 → 피크 → 쿨다운)
    ramp_up: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 10 }, // 워밍업
        { duration: "1m", target: 30 }, // 정상 부하
        { duration: "30s", target: 60 }, // 피크 부하
        { duration: "30s", target: 0 }, // 쿨다운
      ],
    },
  },
  thresholds: {
    // 95번째 백분위 응답시간 < 500ms
    http_req_duration: ["p(95)<500"],
    // 오류율 < 1%
    http_req_failed: ["rate<0.01"],
    // 생성 API 95th < 300ms
    "todo_create_duration": ["p(95)<300"],
    // 조회 API 95th < 200ms
    "todo_find_duration": ["p(95)<200"],
  },
};

// ── 커스텀 메트릭 ────────────────────────────────────────────────────────────

const todoCreateDuration = new Trend("todo_create_duration");
const todoFindDuration = new Trend("todo_find_duration");
const errorRate = new Rate("error_rate");

// ── 헬퍼 ─────────────────────────────────────────────────────────────────────

const headers = { "Content-Type": "application/json" };

function createTodo(title) {
  const start = Date.now();
  const res = http.post(
    `${BASE_URL}/api/todos`,
    JSON.stringify({ title }),
    { headers }
  );
  todoCreateDuration.add(Date.now() - start);

  const ok = check(res, {
    "create: status 201": (r) => r.status === 201,
    "create: has id": (r) => {
      try {
        return JSON.parse(r.body).id !== undefined;
      } catch {
        return false;
      }
    },
  });
  errorRate.add(!ok);
  return ok ? JSON.parse(res.body) : null;
}

function findTodo(id) {
  const start = Date.now();
  const res = http.get(`${BASE_URL}/api/todos/${id}`, { headers });
  todoFindDuration.add(Date.now() - start);

  const ok = check(res, {
    "find: status 200": (r) => r.status === 200,
  });
  errorRate.add(!ok);
  return ok ? JSON.parse(res.body) : null;
}

function listTodos() {
  const start = Date.now();
  const res = http.get(`${BASE_URL}/api/todos`, { headers });
  todoFindDuration.add(Date.now() - start);

  const ok = check(res, {
    "list: status 200": (r) => r.status === 200,
    "list: has Total-Count header": (r) =>
      r.headers["Total-Count"] !== undefined,
  });
  errorRate.add(!ok);
}

function updateTodo(id, completed) {
  const res = http.put(
    `${BASE_URL}/api/todos/${id}`,
    JSON.stringify({ completed }),
    { headers }
  );
  const ok = check(res, {
    "update: status 200": (r) => r.status === 200,
  });
  errorRate.add(!ok);
}

function deleteTodo(id) {
  const res = http.del(`${BASE_URL}/api/todos/${id}`, null, { headers });
  const ok = check(res, {
    "delete: status 204": (r) => r.status === 204,
  });
  errorRate.add(!ok);
}

// ── 메인 시나리오 ─────────────────────────────────────────────────────────────

export default function () {
  // 1. 생성
  const title = `load-test-todo-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
  const created = createTodo(title);
  if (!created) return;

  sleep(0.1);

  // 2. 단건 조회
  findTodo(created.id);

  sleep(0.1);

  // 3. 목록 조회
  listTodos();

  sleep(0.1);

  // 4. 완료 처리
  updateTodo(created.id, true);

  sleep(0.1);

  // 5. 삭제
  deleteTodo(created.id);

  sleep(0.5);
}

// ── 테스트 종료 요약 ──────────────────────────────────────────────────────────

export function handleSummary(data) {
  return {
    stdout: JSON.stringify(
      {
        "http_req_duration_p95_ms":
          data.metrics.http_req_duration?.values?.["p(95)"]?.toFixed(1),
        "http_req_failed_rate_%": (
          (data.metrics.http_req_failed?.values?.rate || 0) * 100
        ).toFixed(2),
        "todo_create_p95_ms":
          data.metrics.todo_create_duration?.values?.["p(95)"]?.toFixed(1),
        "todo_find_p95_ms":
          data.metrics.todo_find_duration?.values?.["p(95)"]?.toFixed(1),
        "vus_max": data.metrics.vus_max?.values?.max,
        "iterations": data.metrics.iterations?.values?.count,
      },
      null,
      2
    ),
  };
}
