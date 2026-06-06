/**
 * CategoryOption Scenarios
 *
 * 흐름:
 *   [사전 준비] 카테고리 생성 → 옵션 생성 → 옵션 id 조회
 *   → 옵션 추가 → 재추가(skip 검증) → 조회 → 제거 → 제거 후 검증
 *   [사후 정리] 옵션 삭제 → 카테고리 삭제
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../lib/auth-helper.js';
import { createCategory, deleteCategory } from '../domains/product/category-service.js';
import { createOption, getAllOptions, deleteOption } from '../domains/product/option-service.js';
import {
  getCategoryOptions,
  addCategoryOption,
  deleteCategoryOption,
} from '../domains/product/category-option-service.js';

function getValidatedToken() {
  const token = getAuthTokenFromEnv();
  if (!token) {
    console.error('[Auth] 토큰 없음. --env TEST_TOKEN=<token>');
    return null;
  }
  return token;
}

/**
 * 카테고리-옵션 통합 시나리오
 */
export function categoryOptionFullScenario() {
  const token = getValidatedToken();
  if (!token) return;

  let categoryId = null;
  let optionId = null;
  let categoryOptionId = null;

  // ── 사전 준비: 카테고리 생성 ────────────────────────────────
  const categoryRes = createCategory(token, { parentId: null, name: 'smoke_카테고리옵션용' });

  const isCategoryCreated = check(categoryRes, {
    '[사전 준비/createCategory] status is 200': (r) => r.status === 200,
    '[사전 준비/createCategory] 응답에 id 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.id === 'number'; } catch { return false; }
    },
  });

  if (!isCategoryCreated) {
    console.error(`[categoryOptionFullScenario] 카테고리 생성 실패 | status: ${categoryRes.status} | body: ${categoryRes.body}`);
    return;
  }

  categoryId = JSON.parse(categoryRes.body).data.id;
  sleep(0.5);

  // ── 사전 준비: 옵션 생성 ────────────────────────────────────
  const optionRes = createOption(token, { name: 'smoke_카테고리옵션용_색상', inputType: 'SELECT' });

  const isOptionCreated = check(optionRes, {
    '[사전 준비/createOption] status is 200': (r) => r.status === 200,
    '[사전 준비/createOption] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  if (!isOptionCreated) {
    console.error(`[categoryOptionFullScenario] 옵션 생성 실패 | status: ${optionRes.status} | body: ${optionRes.body}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── 사전 준비: 옵션 id 조회 ─────────────────────────────────
  try {
    const allOptionsData = JSON.parse(getAllOptions(token).body).data;
    const matched = allOptionsData.find(
        (opt) => opt.name === 'smoke_카테고리옵션용_색상' && opt.inputType === 'SELECT'
    );

    if (!matched) {
      console.error('[categoryOptionFullScenario] 옵션 id 추출 실패');
      deleteCategory(token, categoryId);
      return;
    }

    optionId = matched.id;
  } catch (e) {
    console.error(`[categoryOptionFullScenario] 옵션 id 파싱 실패: ${e}`);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── STEP 1: 카테고리-옵션 추가 ──────────────────────────────
  const addRes = addCategoryOption(token, categoryId, {
    optionIds: [optionId],
    propagateToChildren: false,
    required: false,
  });

  const isAddSuccess = check(addRes, {
    '[addCategoryOption] status is 200': (r) => r.status === 200,
    '[addCategoryOption] categoryId 일치': (r) => {
      try { return JSON.parse(r.body).data.categoryId === categoryId; } catch { return false; }
    },
    '[addCategoryOption] 요청한 옵션이 skip 없이 추가됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedOptions;
        return Array.isArray(skipped) && skipped.length === 0;
      } catch { return false; }
    },
  });

  if (!isAddSuccess) {
    console.error(`[categoryOptionFullScenario] 옵션 추가 실패 | status: ${addRes.status} | body: ${addRes.body}`);
    deleteOption(token, optionId);
    deleteCategory(token, categoryId);
    return;
  }

  sleep(0.5);

  // ── STEP 2: 동일 옵션 재추가 (skip 검증) ────────────────────
  const reAddRes = addCategoryOption(token, categoryId, {
    optionIds: [optionId],
    propagateToChildren: false,
    required: false,
  });

  check(reAddRes, {
    '[addCategoryOption/재추가] status is 200': (r) => r.status === 200,
    '[addCategoryOption/재추가] 이미 존재하는 옵션이 skippedOptions에 포함됨': (r) => {
      try {
        const skipped = JSON.parse(r.body).data.skippedOptions;
        return Array.isArray(skipped) && skipped.some((s) => s.optionId === optionId);
      } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 3: 조회 및 categoryOptionId 추출 ───────────────────
  const getOptRes = getCategoryOptions(token, categoryId);

  check(getOptRes, {
    '[getCategoryOptions] status is 200': (r) => r.status === 200,
    '[getCategoryOptions] categoryId 일치': (r) => {
      try { return JSON.parse(r.body).data.categoryId === categoryId; } catch { return false; }
    },
    '[getCategoryOptions] 추가한 옵션이 목록에 존재': (r) => {
      try {
        return JSON.parse(r.body).data.options.some((opt) => opt.optionId === optionId);
      } catch { return false; }
    },
    '[getCategoryOptions] CategoryOptionDto 구조 검증': (r) => {
      try {
        return JSON.parse(r.body).data.options.every(
            (opt) =>
                typeof opt.categoryOptionId === 'number' &&
                typeof opt.optionId === 'number' &&
                typeof opt.name === 'string' &&
                (opt.inputType === 'SELECT' || opt.inputType === 'RADIO') &&
                typeof opt.required === 'boolean'
        );
      } catch { return false; }
    },
  });

  try {
    const opts = JSON.parse(getOptRes.body).data.options;
    const matched = opts.find((opt) => opt.optionId === optionId);
    if (matched) categoryOptionId = matched.categoryOptionId;
  } catch (e) {
    console.error(`[categoryOptionFullScenario] categoryOptionId 파싱 실패: ${e}`);
  }

  sleep(0.5);

  // ── STEP 4: 카테고리-옵션 제거 ──────────────────────────────
  if (categoryOptionId === null) {
    console.warn('[categoryOptionFullScenario] categoryOptionId 없음. 삭제 건너뜁니다.');
  } else {
    const deleteOptRes = deleteCategoryOption(token, categoryId, categoryOptionId);

    check(deleteOptRes, {
      '[deleteCategoryOption] status is 200': (r) => r.status === 200,
      '[deleteCategoryOption] code is SUCCESS': (r) => {
        try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
      },
    });

    sleep(0.5);

    // ── STEP 5: 제거 후 검증 ──────────────────────────────────
    const verifyRes = getCategoryOptions(token, categoryId);

    check(verifyRes, {
      '[verify] 제거 후 options가 비어있음': (r) => {
        try {
          const opts = JSON.parse(r.body).data.options;
          return Array.isArray(opts) && opts.length === 0;
        } catch { return false; }
      },
    });
  }

  sleep(0.5);

  // ── 사후 정리 ────────────────────────────────────────────────
  deleteOption(token, optionId);
  sleep(0.3);
  deleteCategory(token, categoryId);
  sleep(0.5);
}