/**
 * CategoryAttribute Smoke Test
 *
 * 테스트 내용:
 *   [사전 준비] 카테고리 생성 → 속성 생성 → 속성 id 조회
 *
 *   1. 카테고리-속성 추가 (skip 없이 정상 추가 검증)
 *   2. 카테고리-속성 추가 재시도 (동일 속성 → skippedAttributes 검증)
 *   3. 카테고리 속성 조회 (추가된 속성 및 categoryAttributeId 추출)
 *   4. 카테고리-속성 제거
 *   5. 제거 후 조회로 정리 검증
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/category-attribute-smoke-test.js --env TEST_TOKEN=
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import { createCategory, deleteCategory } from '../../domains/product/category-service.js';
import { createAttribute, getAllAttributes } from '../../domains/product/attribute-service.js';
import {
  getCategoryAttributes,
  addCategoryAttribute,
  deleteCategoryAttribute,
} from '../../domains/product/category-attribute-service.js';

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
  let attributeId = null;
  let categoryAttributeId = null;

  // 사전 준비 STEP 1: 카테고리 생성

  console.log('[Smoke] 사전 준비 1. 카테고리 생성');

  const categoryRes = createCategory(token, {
    parentId: null,
    name: '스모크테스트_카테고리속성용',
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

  // 사전 준비 STEP 2: 속성 생성

  console.log('[Smoke] 사전 준비 2. 속성 생성');

  const attributeRes = createAttribute(token, {
    key: '스모크테스트_카테고리속성용_소재',
    values: ['면', '폴리에스터'],
  });

  console.log(`[Smoke] createAttribute | status: ${attributeRes.status} | body: ${attributeRes.body}`);

  const isAttributeCreated = check(attributeRes, {
    '[사전 준비/createAttribute] status is 200': (r) => r.status === 200,
    '[사전 준비/createAttribute] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  if (!isAttributeCreated) {
    console.error('[Smoke] 속성 생성 실패. 테스트를 중단합니다.');
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // 사전 준비 STEP 3: 생성한 속성의 id 조회

  console.log('[Smoke] 사전 준비 3. 생성한 속성 id 조회');

  const getAllAttrRes = getAllAttributes(token);

  try {
    const allAttrData = JSON.parse(getAllAttrRes.body).data;

    const matched = allAttrData.find(
        (group) => group.key === '스모크테스트_카테고리속성용_소재'
    );

    if (!matched || matched.values.length === 0) {
      console.error('[Smoke] 생성한 속성을 조회하지 못했습니다. 테스트를 중단합니다.');
      deleteCategory(token, categoryId);
      return;
    }

    attributeId = matched.values[0].id;
    console.log(`[Smoke] 속성 id 추출 완료 | attributeId: ${attributeId}`);
  } catch (e) {
    console.error(`[Smoke] 속성 id 파싱 실패: ${e}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // STEP 1: 카테고리-속성 추가

  console.log('[Smoke] 1. 카테고리-속성 추가 요청');

  const addRes = addCategoryAttribute(token, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  console.log(`[Smoke] addCategoryAttribute | status: ${addRes.status} | body: ${addRes.body}`);

  const isAddSuccess = check(addRes, {
    '[addCategoryAttribute] status is 200': (r) => r.status === 200,
    '[addCategoryAttribute] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[addCategoryAttribute] categoryId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.categoryId === categoryId;
      } catch {
        return false;
      }
    },
    // skippedAttributes가 비어있으면 요청한 속성이 정상적으로 추가된 것입니다.
    '[addCategoryAttribute] 요청한 속성이 skip 없이 추가됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedAttributes;
        return Array.isArray(skipped) && skipped.length === 0;
      } catch {
        return false;
      }
    },
  });

  if (!isAddSuccess) {
    console.error('[Smoke] 카테고리-속성 추가 실패. 이후 단계를 건너뜁니다.');
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // STEP 2: 동일 속성 재추가

  console.log('[Smoke] 2. 동일 속성 재추가 요청 (skip 동작 검증)');

  const reAddRes = addCategoryAttribute(token, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  console.log(`[Smoke] addCategoryAttribute (재추가) | status: ${reAddRes.status} | body: ${reAddRes.body}`);

  check(reAddRes, {
    '[addCategoryAttribute/재추가] status is 200': (r) => r.status === 200,
    '[addCategoryAttribute/재추가] 이미 존재하는 속성이 skippedAttributes에 포함됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedAttributes;
        return (
            Array.isArray(skipped) &&
            skipped.some((s) => s.attributeId === attributeId)
        );
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 3: 카테고리 속성 조회 및 categoryAttributeId 추출

  console.log(`[Smoke] 3. 카테고리 속성 조회 | categoryId: ${categoryId}`);

  const getAttrRes = getCategoryAttributes(token, categoryId);

  console.log(`[Smoke] getCategoryAttributes | status: ${getAttrRes.status} | body: ${getAttrRes.body}`);

  check(getAttrRes, {
    '[getCategoryAttributes] status is 200': (r) => r.status === 200,
    '[getCategoryAttributes] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getCategoryAttributes] categoryId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.categoryId === categoryId;
      } catch {
        return false;
      }
    },
    '[getCategoryAttributes] attributes가 배열': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body).data.attributes);
      } catch {
        return false;
      }
    },
    '[getCategoryAttributes] 추가한 속성 키가 존재 (attributeKey)': (r) => {
      try {
        const attributes = JSON.parse(r.body).data.attributes;
        return attributes.some(
            (group) => group.attributeKey === '스모크테스트_카테고리속성용_소재'
        );
      } catch {
        return false;
      }
    },
    '[getCategoryAttributes] attributeValues 구조가 올바름': (r) => {
      try {
        const attributes = JSON.parse(r.body).data.attributes;
        return attributes.every((group) =>
            Array.isArray(group.attributeValues) &&
            group.attributeValues.every(
                (v) =>
                    typeof v.categoryAttributeId === 'number' &&
                    typeof v.attributeId === 'number' &&
                    typeof v.value === 'string' &&
                    typeof v.required === 'boolean'
            )
        );
      } catch {
        return false;
      }
    },
  });

  try {
    const attributes = JSON.parse(getAttrRes.body).data.attributes;
    const matchedGroup = attributes.find(
        (group) => group.attributeKey === '스모크테스트_카테고리속성용_소재'
    );

    if (matchedGroup && matchedGroup.attributeValues.length > 0) {
      categoryAttributeId = matchedGroup.attributeValues[0].categoryAttributeId;
      console.log(`[Smoke] categoryAttributeId 추출 완료 | categoryAttributeId: ${categoryAttributeId}`);
    }
  } catch (e) {
    console.error(`[Smoke] categoryAttributeId 파싱 실패: ${e}`);
  }

  sleep(0.5);

  // STEP 4: 카테고리-속성 제거

  if (categoryAttributeId === null) {
    console.warn('[Smoke] categoryAttributeId를 알 수 없어 삭제 단계를 건너뜁니다.');
  } else {
    console.log(`[Smoke] 4. 카테고리-속성 제거 | categoryAttributeId: ${categoryAttributeId}`);

    const deleteAttrRes = deleteCategoryAttribute(token, categoryId, categoryAttributeId);

    console.log(`[Smoke] deleteCategoryAttribute | status: ${deleteAttrRes.status} | body: ${deleteAttrRes.body}`);

    check(deleteAttrRes, {
      '[deleteCategoryAttribute] status is 200': (r) => r.status === 200,
      '[deleteCategoryAttribute] code is SUCCESS': (r) => {
        try {
          return JSON.parse(r.body).code === 'SUCCESS';
        } catch {
          return false;
        }
      },
    });

    sleep(0.5);

    // STEP 5: 제거 후 조회로 정리 검증

    console.log('[Smoke] 5. 제거 후 카테고리 속성 재조회');

    const verifyRes = getCategoryAttributes(token, categoryId);

    console.log(`[Smoke] getCategoryAttributes (after delete) | status: ${verifyRes.status} | body: ${verifyRes.body}`);

    check(verifyRes, {
      '[verify] status is 200': (r) => r.status === 200,
      '[verify] 제거 후 attributes가 비어있음': (r) => {
        try {
          const attributes = JSON.parse(r.body).data.attributes;
          return Array.isArray(attributes) && attributes.length === 0;
        } catch {
          return false;
        }
      },
    });
  }

  sleep(0.5);

  // 카테고리 삭제

  console.log(`[Smoke] 사후 정리. 카테고리 삭제 | categoryId: ${categoryId}`);

  const cleanupRes = deleteCategory(token, categoryId);

  console.log(`[Smoke] deleteCategory | status: ${cleanupRes.status} | body: ${cleanupRes.body}`);

  check(cleanupRes, {
    '[사후 정리/deleteCategory] status is 200': (r) => r.status === 200,
  });

  console.log('[Smoke] 완료');
}