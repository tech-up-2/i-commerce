/**
 * CategoryAttribute Scenarios
 *
 * 흐름:
 *   [사전 준비] 카테고리 생성 → 속성 생성 → 속성 id 조회
 *   → 속성 추가 → 재추가(skip 검증) → 조회 → 제거 → 제거 후 검증
 *   [사후 정리] 카테고리 삭제
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../lib/auth-helper.js';
import { createCategory, deleteCategory } from '../domains/product/category-service.js';
import { createAttribute, getAllAttributes } from '../domains/product/attribute-service.js';
import {
  getCategoryAttributes,
  addCategoryAttribute,
  deleteCategoryAttribute,
} from '../domains/product/category-attribute-service.js';

function getValidatedToken() {
  const token = getAuthTokenFromEnv();
  if (!token) {
    console.error('[Auth] 토큰 없음. --env TEST_TOKEN=<token>');
    return null;
  }
  return token;
}

/**
 * 카테고리-속성 통합 시나리오
 */
export function categoryAttributeFullScenario() {
  const token = getValidatedToken();
  if (!token) return;

  let categoryId = null;
  let attributeId = null;
  let categoryAttributeId = null;

  // ── 사전 준비: 카테고리 생성 ────────────────────────────────
  const categoryRes = createCategory(token, { parentId: null, name: 'smoke_카테고리속성용' });

  const isCategoryCreated = check(categoryRes, {
    '[사전 준비/createCategory] status is 200': (r) => r.status === 200,
    '[사전 준비/createCategory] 응답에 id 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.id === 'number'; } catch { return false; }
    },
  });

  if (!isCategoryCreated) {
    console.error(`[categoryAttributeFullScenario] 카테고리 생성 실패 | status: ${categoryRes.status} | body: ${categoryRes.body}`);
    return;
  }

  categoryId = JSON.parse(categoryRes.body).data.id;
  sleep(0.5);

  // ── 사전 준비: 속성 생성 ────────────────────────────────────
  const attrKey = 'smoke_카테고리속성용_소재';
  const attributeRes = createAttribute(token, { key: attrKey, values: ['면', '폴리에스터'] });

  const isAttributeCreated = check(attributeRes, {
    '[사전 준비/createAttribute] status is 200': (r) => r.status === 200,
    '[사전 준비/createAttribute] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  if (!isAttributeCreated) {
    console.error(`[categoryAttributeFullScenario] 속성 생성 실패 | status: ${attributeRes.status} | body: ${attributeRes.body}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── 사전 준비: 속성 id 조회 ─────────────────────────────────
  try {
    const allAttrData = JSON.parse(getAllAttributes(token).body).data;
    const matched = allAttrData.find((group) => group.key === attrKey);

    if (!matched || matched.values.length === 0) {
      console.error('[categoryAttributeFullScenario] 속성 id 추출 실패');
      deleteCategory(token, categoryId);
      return;
    }

    attributeId = matched.values[0].id;
  } catch (e) {
    console.error(`[categoryAttributeFullScenario] 속성 id 파싱 실패: ${e}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── STEP 1: 카테고리-속성 추가 ──────────────────────────────
  const addRes = addCategoryAttribute(token, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  const isAddSuccess = check(addRes, {
    '[addCategoryAttribute] status is 200': (r) => r.status === 200,
    '[addCategoryAttribute] categoryId 일치': (r) => {
      try { return JSON.parse(r.body).data.categoryId === categoryId; } catch { return false; }
    },
    '[addCategoryAttribute] 요청한 속성이 skip 없이 추가됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedAttributes;
        return Array.isArray(skipped) && skipped.length === 0;
      } catch { return false; }
    },
  });

  if (!isAddSuccess) {
    console.error(`[categoryAttributeFullScenario] 속성 추가 실패 | status: ${addRes.status} | body: ${addRes.body}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── STEP 2: 동일 속성 재추가 (skip 검증) ────────────────────
  const reAddRes = addCategoryAttribute(token, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  check(reAddRes, {
    '[addCategoryAttribute/재추가] status is 200': (r) => r.status === 200,
    '[addCategoryAttribute/재추가] 이미 존재하는 속성이 skippedAttributes에 포함됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedAttributes;
        return Array.isArray(skipped) && skipped.some((s) => s.attributeId === attributeId);
      } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 3: 조회 및 categoryAttributeId 추출 ────────────────
  const getAttrRes = getCategoryAttributes(token, categoryId);

  check(getAttrRes, {
    '[getCategoryAttributes] status is 200': (r) => r.status === 200,
    '[getCategoryAttributes] categoryId 일치': (r) => {
      try { return JSON.parse(r.body).data.categoryId === categoryId; } catch { return false; }
    },
    '[getCategoryAttributes] 추가한 속성 키가 존재': (r) => {
      try {
        return JSON.parse(r.body).data.attributes.some((g) => g.attributeKey === attrKey);
      } catch { return false; }
    },
    '[getCategoryAttributes] attributeValues 구조 검증': (r) => {
      try {
        return JSON.parse(r.body).data.attributes.every((g) =>
            Array.isArray(g.attributeValues) &&
            g.attributeValues.every(
                (v) =>
                    typeof v.categoryAttributeId === 'number' &&
                    typeof v.attributeId === 'number' &&
                    typeof v.value === 'string' &&
                    typeof v.required === 'boolean'
            )
        );
      } catch { return false; }
    },
  });

  try {
    const attributes = JSON.parse(getAttrRes.body).data.attributes;
    const matchedGroup = attributes.find((g) => g.attributeKey === attrKey);
    if (matchedGroup?.attributeValues.length > 0) {
      categoryAttributeId = matchedGroup.attributeValues[0].categoryAttributeId;
    }
  } catch (e) {
    console.error(`[categoryAttributeFullScenario] categoryAttributeId 파싱 실패: ${e}`);
  }

  sleep(0.5);

  // ── STEP 4: 카테고리-속성 제거 ──────────────────────────────
  if (categoryAttributeId === null) {
    console.warn('[categoryAttributeFullScenario] categoryAttributeId 없음. 삭제 건너뜁니다.');
  } else {
    const deleteAttrRes = deleteCategoryAttribute(token, categoryId, categoryAttributeId);

    check(deleteAttrRes, {
      '[deleteCategoryAttribute] status is 200': (r) => r.status === 200,
      '[deleteCategoryAttribute] code is SUCCESS': (r) => {
        try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
      },
    });

    sleep(0.5);

    // ── STEP 5: 제거 후 검증 ──────────────────────────────────
    const verifyRes = getCategoryAttributes(token, categoryId);

    check(verifyRes, {
      '[verify] 제거 후 attributes가 비어있음': (r) => {
        try {
          return Array.isArray(JSON.parse(r.body).data.attributes) &&
              JSON.parse(r.body).data.attributes.length === 0;
        } catch { return false; }
      },
    });
  }

  sleep(0.5);

  // ── 사후 정리 ────────────────────────────────────────────────
  deleteCategory(token, categoryId);
  sleep(0.5);
}