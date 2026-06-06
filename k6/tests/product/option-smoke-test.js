/**
 * Option Smoke Test
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/option-smoke-test.js --env TEST_TOKEN=
 */
import { check, sleep } from 'k6';
import { createOption, getAllOptions, deleteOption } from '../../domains/product/option-service.js';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const token = getAuthTokenFromEnv();

  if (!token) {
    console.error('[Smoke] 토큰이 없습니다. --env TEST_TOKEN=<token> 을 확인해 주세요.');
    return;
  }

  // STEP 1: 옵션 생성
  console.log('[Smoke] 1. 옵션 생성 요청');

  const createPayload = { name: '스모크테스트_색상', inputType: 'SELECT' };
  const createRes = createOption(token, createPayload);

  console.log(`[Smoke] createOption | status: ${createRes.status} | body: ${createRes.body}`);

  check(createRes, {
    '[createOption] status is 200': (r) => r.status === 200,
    '[createOption] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 2: 전체 옵션 조회

  console.log('[Smoke] 2. 전체 옵션 조회 요청');

  const getAllRes = getAllOptions(token);

  console.log(`[Smoke] getAllOptions | status: ${getAllRes.status} | body: ${getAllRes.body}`);

  let createdOption = null;

  check(getAllRes, {
    '[getAllOptions] status is 200': (r) => r.status === 200,
    '[getAllOptions] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getAllOptions] data is array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body).data);
      } catch {
        return false;
      }
    },
    '[getAllOptions] 생성한 옵션이 목록에 존재': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        createdOption = data
        .filter((opt) => opt.name === createPayload.name && opt.inputType === createPayload.inputType)
        .sort((a, b) => b.id - a.id)[0] || null;

        return createdOption !== null;
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 3: 옵션 삭제

  if (!createdOption) {
    console.warn('[Smoke] 삭제할 옵션 ID를 찾지 못했습니다. 삭제 단계를 건너뜁니다.');
    return;
  }

  console.log(`[Smoke] 3. 옵션 삭제 요청 | optionId: ${createdOption.id}`);

  const deleteRes = deleteOption(token, createdOption.id);

  console.log(`[Smoke] deleteOption | status: ${deleteRes.status} | body: ${deleteRes.body}`);

  check(deleteRes, {
    '[deleteOption] status is 200': (r) => r.status === 200,
    '[deleteOption] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 4: 삭제 후 목록에서 제거됐는지 재확인

  console.log('[Smoke] 4. 삭제 후 목록 재확인');

  const verifyRes = getAllOptions(token);

  check(verifyRes, {
    '[verify] 삭제한 옵션이 목록에서 제거됨': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return !data.some((opt) => opt.id === createdOption.id);
      } catch {
        return false;
      }
    },
  });

  console.log('[Smoke] 완료');
}