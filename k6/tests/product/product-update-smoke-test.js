/**
 * Product Update Smoke Test
 *
 * 상태 전이 확인:
 *   STEP 2. ON_SALE      → PENDING      (정상)
 *   STEP 3. PENDING      → ON_SALE      (정상)
 *   STEP 4. ON_SALE      → DISCONTINUED (정상)
 *   STEP 5. DISCONTINUED → ON_SALE      (불가 → 400 + PRD-40007 검증)
 *   STEP 6. DISCONTINUED → PENDING      (정상 복구)
 *
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/product-update-smoke-test.js \
 *     --env ADMIN_TOKEN=... \
 *     --env TEST_TOKEN=... \
 *     --env TEST_STORE_ID=
 *
 */
import { check, sleep } from 'k6';
import { getAuthTokenFromEnv, getAdminToken } from '../../lib/auth-helper.js';
import { getStoreId } from '../../lib/store-helper.js';
import {
  setupProductTestData,
  cleanupProductTestData,
  createProductNoneFlow,
} from '../../scenarios/product-create-flows.js';
import {
  updateProduct,
  changeProductStatus,
} from '../../domains/product/product-command-service.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const sellerToken = getAuthTokenFromEnv();
  const adminToken = getAdminToken();
  const storeId = getStoreId();

  if (!sellerToken || !adminToken || !storeId) {
    console.error(
        '[Smoke] 필수 환경변수가 없습니다. 실행을 중단합니다.\n' +
        '  --env ADMIN_TOKEN=<관리자 토큰>\n' +
        '  --env TEST_TOKEN=<셀러 토큰>\n' +
        '  --env TEST_STORE_ID=<스토어 ID>'
    );
    return;
  }

  // 사전 준비: 카테고리/옵션/속성 세팅

  console.log('[Smoke] 사전 준비 시작 (관리자 토큰)');

  const testData = setupProductTestData(adminToken);

  if (!testData) {
    console.error('[Smoke] 사전 준비 실패. 테스트를 중단합니다.');
    return;
  }

  console.log('[Smoke] 사전 준비 완료');
  sleep(0.5);

  // 사전 준비: 테스트용 상품 생성

  console.log('[Smoke] 테스트용 상품 생성 (셀러 토큰)');

  const productId = createProductNoneFlow(sellerToken, storeId, testData);

  if (!productId) {
    console.error('[Smoke] 테스트용 상품 생성 실패. 테스트를 중단합니다.');
    cleanupProductTestData(adminToken, testData);
    return;
  }

  console.log(`[Smoke] 테스트용 상품 생성 완료 | productId: ${productId}`);
  sleep(0.5);

  // STEP 1: 상품 기본 정보 수정

  console.log('\n[Smoke] ===== STEP 1: 상품 기본 정보 수정 =====');

  const updateRes = updateProduct(sellerToken, productId, {
    name: '스모크테스트_수정된상품명',
    description: '수정된 상품 설명입니다.',
  });

  console.log(`[Smoke] updateProduct | status: ${updateRes.status} | body: ${updateRes.body}`);

  check(updateRes, {
    '[updateProduct] status is 200': (r) => r.status === 200,
    '[updateProduct] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 2: ON_SALE → PENDING

  console.log('\n[Smoke] ===== STEP 2: ON_SALE → PENDING =====');

  const toPendingRes = changeProductStatus(sellerToken, productId, {
    status: 'PENDING',
  });

  console.log(`[Smoke] changeStatus (ON_SALE→PENDING) | status: ${toPendingRes.status} | body: ${toPendingRes.body}`);

  const isPending = check(toPendingRes, {
    '[changeStatus/ON_SALE→PENDING] status is 200': (r) => r.status === 200,
    '[changeStatus/ON_SALE→PENDING] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[changeStatus/ON_SALE→PENDING] 응답 productId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.productId === productId;
      } catch {
        return false;
      }
    },
    '[changeStatus/ON_SALE→PENDING] 응답 status가 PENDING': (r) => {
      try {
        return JSON.parse(r.body).data.status === 'PENDING';
      } catch {
        return false;
      }
    },
  });

  if (!isPending) {
    console.error('[Smoke] ON_SALE→PENDING 전이 실패. 이후 상태 전이 단계를 건너뜁니다.');
    cleanupProductTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // STEP 3: PENDING → ON_SALE

  console.log('\n[Smoke] ===== STEP 3: PENDING → ON_SALE =====');

  const toOnSaleRes = changeProductStatus(sellerToken, productId, {
    status: 'ON_SALE',
  });

  console.log(`[Smoke] changeStatus (PENDING→ON_SALE) | status: ${toOnSaleRes.status} | body: ${toOnSaleRes.body}`);

  const isOnSale = check(toOnSaleRes, {
    '[changeStatus/PENDING→ON_SALE] status is 200': (r) => r.status === 200,
    '[changeStatus/PENDING→ON_SALE] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[changeStatus/PENDING→ON_SALE] 응답 productId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.productId === productId;
      } catch {
        return false;
      }
    },
    '[changeStatus/PENDING→ON_SALE] 응답 status가 ON_SALE': (r) => {
      try {
        return JSON.parse(r.body).data.status === 'ON_SALE';
      } catch {
        return false;
      }
    },
  });

  if (!isOnSale) {
    console.error('[Smoke] PENDING→ON_SALE 전이 실패. 이후 상태 전이 단계를 건너뜁니다.');
    cleanupProductTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // STEP 4: ON_SALE → DISCONTINUED

  console.log('\n[Smoke] ===== STEP 4: ON_SALE → DISCONTINUED =====');

  const toDiscontinuedRes = changeProductStatus(sellerToken, productId, {
    status: 'DISCONTINUED',
  });

  console.log(`[Smoke] changeStatus (ON_SALE→DISCONTINUED) | status: ${toDiscontinuedRes.status} | body: ${toDiscontinuedRes.body}`);

  const isDiscontinued = check(toDiscontinuedRes, {
    '[changeStatus/ON_SALE→DISCONTINUED] status is 200': (r) => r.status === 200,
    '[changeStatus/ON_SALE→DISCONTINUED] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[changeStatus/ON_SALE→DISCONTINUED] 응답 productId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.productId === productId;
      } catch {
        return false;
      }
    },
    '[changeStatus/ON_SALE→DISCONTINUED] 응답 status가 DISCONTINUED': (r) => {
      try {
        return JSON.parse(r.body).data.status === 'DISCONTINUED';
      } catch {
        return false;
      }
    },
  });

  if (!isDiscontinued) {
    console.error('[Smoke] ON_SALE→DISCONTINUED 전이 실패. 이후 상태 전이 단계를 건너뜁니다.');
    cleanupProductTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // STEP 5: DISCONTINUED → ON_SALE

  console.log('\n[Smoke] ===== STEP 5: DISCONTINUED → ON_SALE (불가 전이 검증) =====');

  const invalidTransitionRes = changeProductStatus(sellerToken, productId, {
    status: 'ON_SALE',
  });

  console.log(`[Smoke] changeStatus (DISCONTINUED→ON_SALE) | status: ${invalidTransitionRes.status} | body: ${invalidTransitionRes.body}`);

  check(invalidTransitionRes, {
    '[changeStatus/DISCONTINUED→ON_SALE] status is 400': (r) => r.status === 400,
    '[changeStatus/DISCONTINUED→ON_SALE] code is PRD-40007': (r) => {
      try {
        return JSON.parse(r.body).code === 'PRD-40007';
      } catch {
        return false;
      }
    },
    '[changeStatus/DISCONTINUED→ON_SALE] 에러 메시지 확인': (r) => {
      try {
        return JSON.parse(r.body).message === '현재 상태에서 허용되지 않는 상태 변경입니다.';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // STEP 6: DISCONTINUED → PENDING

  console.log('\n[Smoke] ===== STEP 6: DISCONTINUED → PENDING (정상 복구) =====');

  const toRecoverRes = changeProductStatus(sellerToken, productId, {
    status: 'PENDING',
  });

  console.log(`[Smoke] changeStatus (DISCONTINUED→PENDING) | status: ${toRecoverRes.status} | body: ${toRecoverRes.body}`);

  check(toRecoverRes, {
    '[changeStatus/DISCONTINUED→PENDING] status is 200': (r) => r.status === 200,
    '[changeStatus/DISCONTINUED→PENDING] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[changeStatus/DISCONTINUED→PENDING] 응답 productId 일치': (r) => {
      try {
        return JSON.parse(r.body).data.productId === productId;
      } catch {
        return false;
      }
    },
    '[changeStatus/DISCONTINUED→PENDING] 응답 status가 PENDING': (r) => {
      try {
        return JSON.parse(r.body).data.status === 'PENDING';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);

  // 사후 정리

  console.log('\n[Smoke] 사후 정리 시작 (관리자 토큰)');
  cleanupProductTestData(adminToken, testData);
  console.log('[Smoke] 완료');
}