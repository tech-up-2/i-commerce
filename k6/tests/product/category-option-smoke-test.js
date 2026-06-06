/**
 * CategoryOption Smoke Test
 *
 * 테스트 내용:
 *   [사전 준비] 카테고리 생성 → 옵션 생성 → 옵션 id 조회
 *
 *   1. 카테고리-옵션 추가 (skip 없이 정상 추가 검증)
 *   2. 카테고리-옵션 추가 재시도 (동일 옵션 → skippedOptions 검증)
 *   3. 카테고리 옵션 조회 (추가된 옵션 및 categoryOptionId 추출)
 *   4. 카테고리-옵션 제거
 *   5. 제거 후 조회로 정리 검증
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/category-option-smoke-test.js --env TEST_TOKEN=
 *
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import { createCategory, deleteCategory } from '../../domains/product/category-service.js';
import { createOption, getAllOptions, deleteOption } from '../../domains/product/option-service.js';
import {
  getCategoryOptions,
  addCategoryOption,
  deleteCategoryOption,
} from '../../domains/product/category-option-service.js';

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

  // 테스트 전반에서 추적할 ID들
  let categoryId = null;
  let optionId = null;
  let categoryOptionId = null;

  // 사전 준비 STEP 1: 카테고리 생성

  console.log('[Smoke] 사전 준비 1. 카테고리 생성');

  const categoryRes = createCategory(token, {
    parentId: null,
    name: '스모크테스트_카테고리옵션용',
  });

  console.log(`[Smoke] createCategory | status: ${categoryRes.status} | body: ${categoryRes.body}`);

  const isCategoryCreated = check(categoryRes, {
    '[사전 준비/createCategory] status is 200': (r) => r.status === 200,
    '[사전 준비/createCategory] 응답에 id 존재': (r) => {
      try {
        return typeof JSON.parse(r.body).data.id === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isCategoryCreated) {
    console.error('[Smoke] 카테고리 생성 실패. 테스트를 중단합니다.');
    return;
  }

  categoryId = JSON.parse(categoryRes.body).data.id;
  console.log(`[Smoke] 카테고리 생성 완료 | categoryId: ${categoryId}`);

  sleep(0.5);

  // 사전 준비 STEP 2: 옵션 생성

  console.log('[Smoke] 사전 준비 2. 옵션 생성');

  const optionRes = createOption(token, {
    name: '스모크테스트_카테고리옵션용_색상',
    inputType: 'SELECT',
  });

  console.log(`[Smoke] createOption | status: ${optionRes.status} | body: ${optionRes.body}`);

  const isOptionCreated = check(optionRes, {
    '[사전 준비/createOption] status is 200': (r) => r.status === 200,
    '[사전 준비/createOption] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  if (!isOptionCreated) {
    console.error('[Smoke] 옵션 생성 실패. 테스트를 중단합니다.');
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // 사전 준비 STEP 3: 생성한 옵션의 id 조회

  console.log('[Smoke] 사전 준비 3. 생성한 옵션 id 조회');

  const getAllOptionsRes = getAllOptions(token);

  try {
    const allOptionsData = JSON.parse(getAllOptionsRes.body).data;

    const matched = allOptionsData.find(
        (opt) =>
            opt.name === '스모크테스트_카테고리옵션용_색상' &&
            opt.inputType === 'SELECT'
    );

    if (!matched) {
      console.error('[Smoke] 생성한 옵션을 조회하지 못했습니다. 테스트를 중단합니다.');
      deleteCategory(token, categoryId);
      return;
    }

    optionId = matched.id;
    console.log(`[Smoke] 옵션 id 추출 완료 | optionId: ${optionId}`);
  } catch (e) {
    console.error(`[Smoke] 옵션 id 파싱 실패: ${e}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // STEP 1: 카테고리-옵션 추가

  console.log('[Smoke] 1. 카테고리-옵션 추가 요청');

  const addRes = addCategoryOption(token, categoryId, {
    optionIds: [optionId],
    propagateToChildren: false,
    required: false,
  });

  console.log(`[Smoke] addCategoryOption | status: ${addRes.status} | body: ${addRes.body}`);

  const isAddSuccess = check(addRes, {
    '[addCategoryOption] status is 200': (r) => r.status === 200,
    '[addCategoryOption] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[addCategoryOption] categoryId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.categoryId === categoryId;
      } catch {
        return false;
      }
    },

    '[addCategoryOption] 요청한 옵션이 skip 없이 추가됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedOptions;
        return Array.isArray(skipped) && skipped.length === 0;
      } catch {
        return false;
      }
    },
  });

  if (!isAddSuccess) {
    console.error('[Smoke] 카테고리-옵션 추가 실패. 이후 단계를 건너뜁니다.');
    deleteOption(token, optionId);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // STEP 2: 동일 옵션 재추가

  console.log('[Smoke] 2. 동일 옵션 재추가 요청 (skip 동작 검증)');

  const reAddRes = addCategoryOption(token, categoryId, {
    optionIds: [optionId],
    propagateToChildren: false,
    required: false,
  });

  console.log(`[Smoke] addCategoryOption (재추가) | status: ${reAddRes.status} | body: ${reAddRes.body}`);

  check(reAddRes, {
    '[addCategoryOption/재추가] status is 200': (r) => r.status === 200,
    '[addCategoryOption/재추가] 이미 존재하는 옵션이 skippedOptions에 포함됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedOptions;
        return (
            Array.isArray(skipped) &&
            skipped.some((s) => s.optionId === optionId)
        );
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 3: 카테고리 옵션 조회 및 categoryOptionId 추출

  console.log(`[Smoke] 3. 카테고리 옵션 조회 | categoryId: ${categoryId}`);

  const getOptRes = getCategoryOptions(token, categoryId);

  console.log(`[Smoke] getCategoryOptions | status: ${getOptRes.status} | body: ${getOptRes.body}`);

  check(getOptRes, {
    '[getCategoryOptions] status is 200': (r) => r.status === 200,
    '[getCategoryOptions] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getCategoryOptions] categoryId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.categoryId === categoryId;
      } catch {
        return false;
      }
    },
    '[getCategoryOptions] options가 배열': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body).data.options);
      } catch {
        return false;
      }
    },
    '[getCategoryOptions] 추가한 옵션이 목록에 존재': (r) => {
      try {
        const opts = JSON.parse(r.body).data.options;
        return opts.some((opt) => opt.optionId === optionId);
      } catch {
        return false;
      }
    },

    '[getCategoryOptions] CategoryOptionDto 구조가 올바름': (r) => {
      try {
        const opts = JSON.parse(r.body).data.options;
        return opts.every(
            (opt) =>
                typeof opt.categoryOptionId === 'number' &&
                typeof opt.optionId === 'number' &&
                typeof opt.name === 'string' &&
                (opt.inputType === 'SELECT' || opt.inputType === 'RADIO') &&
                typeof opt.required === 'boolean'
        );
      } catch {
        return false;
      }
    },
  });

  try {
    const opts = JSON.parse(getOptRes.body).data.options;
    const matched = opts.find((opt) => opt.optionId === optionId);

    if (matched) {
      categoryOptionId = matched.categoryOptionId;
      console.log(`[Smoke] categoryOptionId 추출 완료 | categoryOptionId: ${categoryOptionId}`);
    } else {
      console.warn('[Smoke] 추가한 optionId와 일치하는 항목을 찾지 못했습니다.');
    }
  } catch (e) {
    console.error(`[Smoke] categoryOptionId 파싱 실패: ${e}`);
  }

  sleep(0.5);

  // STEP 4: 카테고리-옵션 제거

  if (categoryOptionId === null) {
    console.warn('[Smoke] categoryOptionId를 알 수 없어 삭제 단계를 건너뜁니다.');
  } else {
    console.log(`[Smoke] 4. 카테고리-옵션 제거 | categoryOptionId: ${categoryOptionId}`);

    const deleteOptRes = deleteCategoryOption(token, categoryId, categoryOptionId);

    console.log(`[Smoke] deleteCategoryOption | status: ${deleteOptRes.status} | body: ${deleteOptRes.body}`);

    check(deleteOptRes, {
      '[deleteCategoryOption] status is 200': (r) => r.status === 200,
      '[deleteCategoryOption] code is SUCCESS': (r) => {
        try {
          return JSON.parse(r.body).code === 'SUCCESS';
        } catch {
          return false;
        }
      },
    });

    sleep(0.5);

    // STEP 5: 제거 후 조회로 정리 검증

    console.log('[Smoke] 5. 제거 후 카테고리 옵션 재조회');

    const verifyRes = getCategoryOptions(token, categoryId);

    console.log(`[Smoke] getCategoryOptions (after delete) | status: ${verifyRes.status} | body: ${verifyRes.body}`);

    check(verifyRes, {
      '[verify] status is 200': (r) => r.status === 200,
      '[verify] 제거 후 options가 비어있음': (r) => {
        try {
          const opts = JSON.parse(r.body).data.options;
          return Array.isArray(opts) && opts.length === 0;
        } catch {
          return false;
        }
      },
    });
  }

  sleep(0.5);

  // ── 사후 정리: 옵션 삭제 → 카테고리 삭제

  console.log(`[Smoke] 사후 정리 1. 옵션 삭제 | optionId: ${optionId}`);

  const deleteOptCleanupRes = deleteOption(token, optionId);

  console.log(`[Smoke] deleteOption | status: ${deleteOptCleanupRes.status} | body: ${deleteOptCleanupRes.body}`);

  check(deleteOptCleanupRes, {
    '[사후 정리/deleteOption] status is 200': (r) => r.status === 200,
  });

  sleep(0.3);

  console.log(`[Smoke] 사후 정리 2. 카테고리 삭제 | categoryId: ${categoryId}`);

  const deleteCategoryCleanupRes = deleteCategory(token, categoryId);

  console.log(`[Smoke] deleteCategory | status: ${deleteCategoryCleanupRes.status} | body: ${deleteCategoryCleanupRes.body}`);

  check(deleteCategoryCleanupRes, {
    '[사후 정리/deleteCategory] status is 200': (r) => r.status === 200,
  });

  console.log('[Smoke] 완료');
}