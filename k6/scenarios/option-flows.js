/**
 * Option Scenarios
 *
 * 흐름:
 *   생성 → 전체 조회 → 동일 재추가(skip 검증) → 삭제 → 삭제 후 검증
 */
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { getAuthTokenFromEnv } from '../lib/auth-helper.js';
import {
  createOption,
  getAllOptions,
  deleteOption,
} from '../domains/product/option-service.js';

const optionItems = new SharedArray('optionItems', function () {
  return JSON.parse(open('../data/option-items.json'));
});

function getValidatedToken() {
  const token = getAuthTokenFromEnv();
  if (!token) {
    console.error('[Auth] 토큰 없음. --env TEST_TOKEN=<token>');
    return null;
  }
  return token;
}

export function optionLifecycleFlow() {
  const token = getValidatedToken();
  if (!token) return;

  const payload = optionItems[Math.floor(Math.random() * optionItems.length)];

  const createRes = createOption(token, payload);

  const isCreateSuccess = check(createRes, {
    '[createOption] status is 200': (r) => r.status === 200,
    '[createOption] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  if (!isCreateSuccess) {
    console.error(`[optionLifecycleFlow] createOption 실패 | status: ${createRes.status} | body: ${createRes.body}`);
    return;
  }

  sleep(0.5);

  const getAllRes = getAllOptions(token);

  check(getAllRes, {
    '[getAllOptions] status is 200': (r) => r.status === 200,
    '[getAllOptions] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[getAllOptions] data is array': (r) => {
      try { return Array.isArray(JSON.parse(r.body).data); } catch { return false; }
    },
  });

  sleep(0.5);

  let targetId = null;

  try {
    const allData = JSON.parse(getAllRes.body).data;
    const matched = allData
    .filter((opt) => opt.name === payload.name && opt.inputType === payload.inputType)
    .sort((a, b) => b.id - a.id);

    if (matched.length > 0) targetId = matched[0].id;
  } catch (e) {
    console.error(`[optionLifecycleFlow] optionId 파싱 실패: ${e}`);
  }

  if (targetId === null) {
    console.warn('[optionLifecycleFlow] 삭제 대상 optionId를 찾지 못했습니다.');
    return;
  }

  const deleteRes = deleteOption(token, targetId);

  check(deleteRes, {
    '[deleteOption] status is 200': (r) => r.status === 200,
    '[deleteOption] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  sleep(1);
}

export function optionReadFlow() {
  const token = getValidatedToken();
  if (!token) return;

  const res = getAllOptions(token);

  check(res, {
    '[getAllOptions] status is 200': (r) => r.status === 200,
    '[getAllOptions] response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}

/**
 * 옵션 통합 시나리오
 * 생성 → 전체 조회 → 삭제 → 삭제 후 목록 검증
 */
export function optionFullScenario() {
  const token = getValidatedToken();
  if (!token) return;

  // ── STEP 1: 옵션 생성 ────────────────────────────────────────
  const createPayload = { name: 'smoke_옵션_색상', inputType: 'SELECT' };
  const createRes = createOption(token, createPayload);

  const isCreateSuccess = check(createRes, {
    '[createOption] status is 200': (r) => r.status === 200,
    '[createOption] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  if (!isCreateSuccess) {
    console.error(`[optionFullScenario] 생성 실패 | status: ${createRes.status} | body: ${createRes.body}`);
    return;
  }

  sleep(0.5);

  // ── STEP 2: 전체 조회 ────────────────────────────────────────
  const getAllRes = getAllOptions(token);

  check(getAllRes, {
    '[getAllOptions] status is 200': (r) => r.status === 200,
    '[getAllOptions] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[getAllOptions] data is array': (r) => {
      try { return Array.isArray(JSON.parse(r.body).data); } catch { return false; }
    },
    '[getAllOptions] 생성한 옵션이 목록에 존재': (r) => {
      try {
        return JSON.parse(r.body).data.some(
            (opt) => opt.name === createPayload.name && opt.inputType === createPayload.inputType
        );
      } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 3: 생성한 옵션 id 추출 후 삭제 ─────────────────────
  let targetId = null;

  try {
    const allData = JSON.parse(getAllRes.body).data;
    const matched = allData
    .filter((opt) => opt.name === createPayload.name && opt.inputType === createPayload.inputType)
    .sort((a, b) => b.id - a.id);

    if (matched.length > 0) targetId = matched[0].id;
  } catch (e) {
    console.error(`[optionFullScenario] optionId 파싱 실패: ${e}`);
    return;
  }

  if (targetId === null) {
    console.warn('[optionFullScenario] 삭제 대상을 찾지 못했습니다.');
    return;
  }

  const deleteRes = deleteOption(token, targetId);

  check(deleteRes, {
    '[deleteOption] status is 200': (r) => r.status === 200,
    '[deleteOption] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 4: 삭제 후 목록 재조회 검증 ────────────────────────
  const verifyRes = getAllOptions(token);

  check(verifyRes, {
    '[verify] 삭제한 옵션이 목록에서 제거됨': (r) => {
      try {
        return !JSON.parse(r.body).data.some((opt) => opt.id === targetId);
      } catch { return false; }
    },
  });

  sleep(0.5);
}