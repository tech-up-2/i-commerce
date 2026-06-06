/**
 * Attribute Smoke Test
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/attribute-smoke-test.js --env TEST_TOKEN=
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import { createAttribute, getAllAttributes } from '../../domains/product/attribute-service.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {

  const token = getAuthTokenFromEnv();

  // STEP 1: 속성 생성

  console.log('[Smoke] 1. 속성 생성 요청');

  const createPayload = {
    key: '스모크테스트_소재',
    values: ['면', '폴리에스터', '울'],
  };

  const createRes = createAttribute(token, createPayload);

  console.log(`[Smoke] createAttribute | status: ${createRes.status} | body: ${createRes.body}`);

  const isCreateSuccess = check(createRes, {
    '[createAttribute] status is 200': (r) => r.status === 200,
    '[createAttribute] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  if (!isCreateSuccess) {
    console.error('[Smoke] 속성 생성 실패. 이후 단계를 건너뜁니다.');
    return;
  }

  sleep(0.5);

  // STEP 2: 전체 속성 조회

  console.log('[Smoke] 2. 전체 속성 조회 요청');

  const getAllRes = getAllAttributes(token);

  console.log(`[Smoke] getAllAttributes | status: ${getAllRes.status} | body: ${getAllRes.body}`);

  check(getAllRes, {
    '[getAllAttributes] status is 200': (r) => r.status === 200,
    '[getAllAttributes] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getAllAttributes] data is array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body).data);
      } catch {
        return false;
      }
    },

    '[getAllAttributes] 응답 구조가 올바름 (key, values 존재)': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return data.every(
            (group) =>
                typeof group.key === 'string' &&
                Array.isArray(group.values) &&
                group.values.every(
                    (v) => typeof v.id === 'number' && typeof v.value === 'string'
                )
        );
      } catch {
        return false;
      }
    },

    '[getAllAttributes] 생성한 속성 키가 목록에 존재': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return data.some((group) => group.key === createPayload.key);
      } catch {
        return false;
      }
    },

    '[getAllAttributes] 생성한 속성 값들이 목록에 존재': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        const matched = data.find((group) => group.key === createPayload.key);

        if (!matched) return false;

        const returnedValues = matched.values.map((v) => v.value);
        return createPayload.values.every((v) => returnedValues.includes(v));
      } catch {
        return false;
      }
    },
  });

  console.log('[Smoke] 완료');
}