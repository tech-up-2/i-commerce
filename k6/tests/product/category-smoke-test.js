/**
 * Category Smoke Test
 *
 * 테스트 내용 :
 *   1. 최상위 카테고리 생성 (parentId: null)
 *   2. 자식 카테고리 생성 (parentId: 1번에서 생성된 id)
 *   3. 전체 카테고리 조회 (트리 구조 검증)
 *   4. 전체 카테고리 조회 with maxDepth (깊이 제한 검증)
 *   5. 특정 카테고리 단건 조회
 *   6. 자식 카테고리 삭제
 *   7. 최상위 카테고리 삭제
 *   8. 삭제 후 조회로 정리 검증
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/category-smoke-test.js --env TEST_TOKEN=
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import {
  createCategory,
  getAllCategories,
  getCategory,
  deleteCategory,
} from '../../domains/product/category-service.js';

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

  let parentCategoryId = null;
  let childCategoryId = null;

  // STEP 1: 최상위 카테고리 생성

  console.log('[Smoke] 1. 최상위 카테고리 생성 요청');

  const parentCreateRes = createCategory(token, {
    parentId: null,
    name: '스모크테스트_의류',
  });

  console.log(`[Smoke] createCategory (parent) | status: ${parentCreateRes.status} | body: ${parentCreateRes.body}`);

  const isParentCreateSuccess = check(parentCreateRes, {
    '[createCategory/parent] status is 200': (r) => r.status === 200,
    '[createCategory/parent] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[createCategory/parent] 응답에 id 존재': (r) => {
      try {
        const id = JSON.parse(r.body).data.id;
        return typeof id === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isParentCreateSuccess) {
    console.error('[Smoke] 최상위 카테고리 생성 실패. 이후 단계를 건너뜁니다.');
    return;
  }

  parentCategoryId = JSON.parse(parentCreateRes.body).data.id;
  console.log(`[Smoke] 최상위 카테고리 생성 완료 | parentCategoryId: ${parentCategoryId}`);

  sleep(0.5);

  // STEP 2: 자식 카테고리 생성

  console.log('[Smoke] 2. 자식 카테고리 생성 요청');

  const childCreateRes = createCategory(token, {
    parentId: parentCategoryId,
    name: '스모크테스트_상의',
  });

  console.log(`[Smoke] createCategory (child) | status: ${childCreateRes.status} | body: ${childCreateRes.body}`);

  const isChildCreateSuccess = check(childCreateRes, {
    '[createCategory/child] status is 200': (r) => r.status === 200,
    '[createCategory/child] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[createCategory/child] 응답에 id 존재': (r) => {
      try {
        const id = JSON.parse(r.body).data.id;
        return typeof id === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isChildCreateSuccess) {
    console.error('[Smoke] 자식 카테고리 생성 실패. 이후 단계를 건너뜁니다.');
    return;
  }

  childCategoryId = JSON.parse(childCreateRes.body).data.id;
  console.log(`[Smoke] 자식 카테고리 생성 완료 | childCategoryId: ${childCategoryId}`);

  sleep(0.5);

  // STEP 3: 전체 카테고리 조회 (트리 구조 검증)

  console.log('[Smoke] 3. 전체 카테고리 조회 요청');

  const getAllRes = getAllCategories(token);

  console.log(`[Smoke] getAllCategories | status: ${getAllRes.status} | body: ${getAllRes.body}`);

  check(getAllRes, {
    '[getAllCategories] status is 200': (r) => r.status === 200,
    '[getAllCategories] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getAllCategories] data is array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body).data);
      } catch {
        return false;
      }
    },
    '[getAllCategories] 응답 구조가 올바름 (id, name, depth, children 존재)': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return data.every(
            (category) =>
                typeof category.id === 'number' &&
                typeof category.name === 'string' &&
                typeof category.depth === 'number' &&
                Array.isArray(category.children)
        );
      } catch {
        return false;
      }
    },
    '[getAllCategories] 생성한 최상위 카테고리가 목록에 존재': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return data.some((category) => category.id === parentCategoryId);
      } catch {
        return false;
      }
    },
    '[getAllCategories] 생성한 자식 카테고리가 트리에 존재': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        const parent = data.find((category) => category.id === parentCategoryId);

        if (!parent) return false;

        return parent.children.some((child) => child.id === childCategoryId);
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 4: 전체 카테고리 조회

  console.log('[Smoke] 4. 전체 카테고리 조회 with maxDepth=0 요청');

  const getAllWithDepthRes = getAllCategories(token, 0);

  console.log(`[Smoke] getAllCategories (maxDepth=0) | status: ${getAllWithDepthRes.status} | body: ${getAllWithDepthRes.body}`);

  check(getAllWithDepthRes, {
    '[getAllCategories/maxDepth=0] status is 200': (r) => r.status === 200,
    '[getAllCategories/maxDepth=0] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getAllCategories/maxDepth=0] children이 비어있음': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return data.every((category) => category.children.length === 0);
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 5: 특정 카테고리 단건 조회

  console.log(`[Smoke] 5. 특정 카테고리 단건 조회 요청 | categoryId: ${parentCategoryId}`);

  const getOneRes = getCategory(token, parentCategoryId);

  console.log(`[Smoke] getCategory | status: ${getOneRes.status} | body: ${getOneRes.body}`);

  check(getOneRes, {
    '[getCategory] status is 200': (r) => r.status === 200,
    '[getCategory] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[getCategory] 조회한 id가 요청한 id와 일치': (r) => {
      try {
        return JSON.parse(r.body).data.id === parentCategoryId;
      } catch {
        return false;
      }
    },
    '[getCategory] 조회한 name이 생성 시 name과 일치': (r) => {
      try {
        return JSON.parse(r.body).data.name === '스모크테스트_의류';
      } catch {
        return false;
      }
    },
    '[getCategory] depth가 0 (최상위 카테고리)': (r) => {
      try {
        return JSON.parse(r.body).data.depth === 0;
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 6: 자식 카테고리 삭제

  console.log(`[Smoke] 6. 자식 카테고리 삭제 요청 | categoryId: ${childCategoryId}`);

  const deleteChildRes = deleteCategory(token, childCategoryId);

  console.log(`[Smoke] deleteCategory (child) | status: ${deleteChildRes.status} | body: ${deleteChildRes.body}`);

  check(deleteChildRes, {
    '[deleteCategory/child] status is 200': (r) => r.status === 200,
    '[deleteCategory/child] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 7: 최상위 카테고리 삭제

  console.log(`[Smoke] 7. 최상위 카테고리 삭제 요청 | categoryId: ${parentCategoryId}`);

  const deleteParentRes = deleteCategory(token, parentCategoryId);

  console.log(`[Smoke] deleteCategory (parent) | status: ${deleteParentRes.status} | body: ${deleteParentRes.body}`);

  check(deleteParentRes, {
    '[deleteCategory/parent] status is 200': (r) => r.status === 200,
    '[deleteCategory/parent] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 8: 삭제 후 단건 조회로 정리 검증

  console.log(`[Smoke] 8. 삭제 후 단건 조회로 정리 검증 | categoryId: ${parentCategoryId}`);

  const verifyRes = getCategory(token, parentCategoryId);

  console.log(`[Smoke] getCategory (after delete) | status: ${verifyRes.status} | body: ${verifyRes.body}`);

  check(verifyRes, {
    '[verify] 삭제된 카테고리 조회 시 404 반환': (r) => r.status === 404,
  });

  console.log('[Smoke] 완료');
}