/**
 * Attribute Scenarios
 *
 * 흐름:
 *   생성 → 전체 조회 → 생성한 속성 키/값 존재 검증
 *
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import { createAttribute, getAllAttributes } from '../../domains/product/attribute-service.js';

function getValidatedToken() {
  const token = getAuthTokenFromEnv();
  if (!token) {
    console.error('[Auth] 토큰 없음. --env TEST_TOKEN=<token>');
    return null;
  }
  return token;
}

/**
 * 속성 통합 시나리오
 * 생성 → 전체 조회 → 생성한 키/값 존재 검증
 */
export function attributeFullScenario() {
  const token = getValidatedToken();
  if (!token) return;

  // ── STEP 1: 속성 생성 ────────────────────────────────────────
  const createPayload = {
    key: 'smoke_소재',
    values: ['면', '폴리에스터', '울'],
  };

  const createRes = createAttribute(token, createPayload);

  const isCreateSuccess = check(createRes, {
    '[createAttribute] status is 200': (r) => r.status === 200,
    '[createAttribute] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  if (!isCreateSuccess) {
    console.error(`[attributeFullScenario] 생성 실패 | status: ${createRes.status} | body: ${createRes.body}`);
    return;
  }

  sleep(0.5);

  // ── STEP 2: 전체 조회 ────────────────────────────────────────
  const getAllRes = getAllAttributes(token);

  check(getAllRes, {
    '[getAllAttributes] status is 200': (r) => r.status === 200,
    '[getAllAttributes] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[getAllAttributes] data is array': (r) => {
      try { return Array.isArray(JSON.parse(r.body).data); } catch { return false; }
    },
    '[getAllAttributes] 응답 구조가 올바름 (key, values 존재)': (r) => {
      try {
        return JSON.parse(r.body).data.every(
            (group) =>
                typeof group.key === 'string' &&
                Array.isArray(group.values) &&
                group.values.every(
                    (v) => typeof v.id === 'number' && typeof v.value === 'string'
                )
        );
      } catch { return false; }
    },
    '[getAllAttributes] 생성한 속성 키가 목록에 존재': (r) => {
      try {
        return JSON.parse(r.body).data.some((group) => group.key === createPayload.key);
      } catch { return false; }
    },
    '[getAllAttributes] 생성한 속성 값들이 목록에 존재': (r) => {
      try {
        const matched = JSON.parse(r.body).data.find((g) => g.key === createPayload.key);
        if (!matched) return false;
        const returnedValues = matched.values.map((v) => v.value);
        return createPayload.values.every((v) => returnedValues.includes(v));
      } catch { return false; }
    },
  });

  sleep(0.5);
}