/**
 * Category Scenarios
 *
 * 흐름:
 *   최상위 카테고리 생성 → 자식 카테고리 생성
 *   → 전체 조회(트리 구조) → maxDepth 조회 → 단건 조회
 *   → 자식 삭제 → 부모 삭제 → 삭제 검증
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import {
  createCategory,
  getAllCategories,
  getCategory,
  deleteCategory,
} from '../../domains/product/category-service.js';

function getValidatedToken() {
  const token = getAuthTokenFromEnv();
  if (!token) {
    console.error('[Auth] 토큰 없음. --env TEST_TOKEN=<token>');
    return null;
  }
  return token;
}

/**
 * 카테고리 통합 시나리오
 */
export function categoryFullScenario() {
  const token = getValidatedToken();
  if (!token) return;

  let parentCategoryId = null;
  let childCategoryId = null;

  // ── STEP 1: 최상위 카테고리 생성 ────────────────────────────
  const parentRes = createCategory(token, { parentId: null, name: 'smoke_의류' });

  const isParentCreated = check(parentRes, {
    '[createCategory/parent] status is 200': (r) => r.status === 200,
    '[createCategory/parent] 응답에 id 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.id === 'number'; } catch { return false; }
    },
  });

  if (!isParentCreated) {
    console.error(`[categoryFullScenario] 최상위 카테고리 생성 실패 | status: ${parentRes.status} | body: ${parentRes.body}`);
    return;
  }

  parentCategoryId = JSON.parse(parentRes.body).data.id;
  sleep(0.5);

  // ── STEP 2: 자식 카테고리 생성 ──────────────────────────────
  const childRes = createCategory(token, { parentId: parentCategoryId, name: 'smoke_상의' });

  const isChildCreated = check(childRes, {
    '[createCategory/child] status is 200': (r) => r.status === 200,
    '[createCategory/child] 응답에 id 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.id === 'number'; } catch { return false; }
    },
  });

  if (!isChildCreated) {
    console.error(`[categoryFullScenario] 자식 카테고리 생성 실패 | status: ${childRes.status} | body: ${childRes.body}`);
    deleteCategory(token, parentCategoryId);
    return;
  }

  childCategoryId = JSON.parse(childRes.body).data.id;
  sleep(0.5);

  // ── STEP 3: 전체 조회 (트리 구조 검증) ──────────────────────
  const getAllRes = getAllCategories(token);

  check(getAllRes, {
    '[getAllCategories] status is 200': (r) => r.status === 200,
    '[getAllCategories] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[getAllCategories] 응답 구조 검증 (id, name, depth, children)': (r) => {
      try {
        return JSON.parse(r.body).data.every(
            (c) =>
                typeof c.id === 'number' &&
                typeof c.name === 'string' &&
                typeof c.depth === 'number' &&
                Array.isArray(c.children)
        );
      } catch { return false; }
    },
    '[getAllCategories] 생성한 최상위 카테고리가 목록에 존재': (r) => {
      try {
        return JSON.parse(r.body).data.some((c) => c.id === parentCategoryId);
      } catch { return false; }
    },
    '[getAllCategories] 생성한 자식 카테고리가 트리에 존재': (r) => {
      try {
        const parent = JSON.parse(r.body).data.find((c) => c.id === parentCategoryId);
        return parent?.children.some((c) => c.id === childCategoryId);
      } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 4: maxDepth=0 조회 ──────────────────────────────────
  const depthRes = getAllCategories(token, 0);

  check(depthRes, {
    '[getAllCategories/maxDepth=0] status is 200': (r) => r.status === 200,
    '[getAllCategories/maxDepth=0] children이 비어있음': (r) => {
      try {
        return JSON.parse(r.body).data.every((c) => c.children.length === 0);
      } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 5: 단건 조회 ────────────────────────────────────────
  const getOneRes = getCategory(token, parentCategoryId);

  check(getOneRes, {
    '[getCategory] status is 200': (r) => r.status === 200,
    '[getCategory] 조회한 id가 요청한 id와 일치': (r) => {
      try { return JSON.parse(r.body).data.id === parentCategoryId; } catch { return false; }
    },
    '[getCategory] 조회한 name이 생성 시 name과 일치': (r) => {
      try { return JSON.parse(r.body).data.name === 'smoke_의류'; } catch { return false; }
    },
    '[getCategory] depth가 0 (최상위)': (r) => {
      try { return JSON.parse(r.body).data.depth === 0; } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 6: 자식 → 부모 순서로 삭제 ────────────────────────
  const deleteChildRes = deleteCategory(token, childCategoryId);

  check(deleteChildRes, {
    '[deleteCategory/child] status is 200': (r) => r.status === 200,
    '[deleteCategory/child] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  sleep(0.5);

  const deleteParentRes = deleteCategory(token, parentCategoryId);

  check(deleteParentRes, {
    '[deleteCategory/parent] status is 200': (r) => r.status === 200,
    '[deleteCategory/parent] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });

  sleep(0.5);

  // ── STEP 7: 삭제 후 단건 조회 검증 ──────────────────────────
  const verifyRes = getCategory(token, parentCategoryId);

  check(verifyRes, {
    '[verify] 삭제된 카테고리 조회 시 404 반환': (r) => r.status === 404,
  });

  sleep(0.5);
}