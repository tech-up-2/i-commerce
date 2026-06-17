/**
 * Product Update Scenarios
 *
 * 흐름:
 *   [사전 준비] 카테고리/옵션/속성 세팅 → NONE 타입 상품 생성
 *       ↓
 *   STEP 1. 상품 기본 정보 수정
 *   STEP 2. ON_SALE → PENDING     (정상)
 *   STEP 3. PENDING → ON_SALE     (정상)
 *   STEP 4. ON_SALE → DISCONTINUED (정상)
 *   STEP 5. DISCONTINUED → ON_SALE (불가 → 400 + PRD-40007)
 *   STEP 6. DISCONTINUED → PENDING (정상 복구)
 *       ↓
 *   [사후 정리] 카테고리/옵션 삭제
 *
 * 초기 상태: ON_SALE (createProduct 직후 기본값)
 */
import { check, sleep } from 'k6';
import { createProduct, updateProduct, changeProductStatus }
  from '../../domains/product/product-command-service.js';
import { setupTestData, cleanupTestData } from '../../lib/product-helper.js';
import { buildNoneProductPayload } from '../../lib/product-payload-builder.js';

/**
 * 상품 기본 정보 수정 플로우
 *
 * @param {string} sellerToken
 * @param {number} productId
 * @returns {boolean} 성공 여부
 */
function updateProductInfoFlow(sellerToken, productId) {
  const res = updateProduct(sellerToken, productId, {
    name: 'smoke_수정된상품명',
    description: '수정된 상품 설명입니다.',
  });

  return check(res, {
    '[updateProduct] status is 200': (r) => r.status === 200,
    '[updateProduct] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
  });
}

/**
 * 상태 전이 플로우 (정상 전이)
 *
 * @param {string} sellerToken
 * @param {number} productId
 * @param {string} fromStatus
 * @param {string} toStatus
 * @returns {boolean} 성공 여부
 */
function changeStatusFlow(sellerToken, productId, fromStatus, toStatus) {
  const res = changeProductStatus(sellerToken, productId, { status: toStatus });

  return check(res, {
    [`[changeStatus/${fromStatus}→${toStatus}] status is 200`]: (r) => r.status === 200,
    [`[changeStatus/${fromStatus}→${toStatus}] code is SUCCESS`]: (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    [`[changeStatus/${fromStatus}→${toStatus}] 응답 productId 일치`]: (r) => {
      try { return JSON.parse(r.body).data.productId === productId; } catch { return false; }
    },
    [`[changeStatus/${fromStatus}→${toStatus}] 응답 status가 ${toStatus}`]: (r) => {
      try { return JSON.parse(r.body).data.status === toStatus; } catch { return false; }
    },
  });
}

/**
 * 상태 전이 플로우 (불가 전이 → 에러 검증)
 *
 * @param {string} sellerToken
 * @param {number} productId
 * @param {string} fromStatus
 * @param {string} toStatus
 */
function changeStatusInvalidFlow(sellerToken, productId, fromStatus, toStatus) {
  const res = changeProductStatus(sellerToken, productId, { status: toStatus });

  check(res, {
    [`[changeStatus/${fromStatus}→${toStatus}] status is 400`]: (r) => r.status === 400,
    [`[changeStatus/${fromStatus}→${toStatus}] code is PRD-40007`]: (r) => {
      try { return JSON.parse(r.body).code === 'PRD-40007'; } catch { return false; }
    },
    [`[changeStatus/${fromStatus}→${toStatus}] 에러 메시지 확인`]: (r) => {
      try {
        return JSON.parse(r.body).message === '현재 상태에서 허용되지 않는 상태 변경입니다.';
      } catch { return false; }
    },
  });
}

/**
 * 상품 수정/상태 변경 통합 시나리오
 *
 * @param {string} adminToken
 * @param {string} sellerToken
 * @param {number} storeId
 */
export function productUpdateFullScenario(adminToken, sellerToken, storeId) {

  // 사전 준비: 카테고리/옵션/속성 세팅
  const testData = setupTestData(adminToken);

  if (!testData) {
    console.error('[productUpdateFullScenario] 사전 준비 실패.');
    return;
  }

  sleep(0.5);

  // 사전 준비: 테스트용 상품 생성
  const createRes = createProduct(sellerToken, buildNoneProductPayload(storeId, testData));

  if (createRes.status !== 200) {
    console.error(`[productUpdateFullScenario] 상품 생성 실패 | status: ${createRes.status} | body: ${createRes.body}`);
    cleanupTestData(adminToken, testData);
    return;
  }

  const productId = JSON.parse(createRes.body).data.productId;
  sleep(0.5);

  // ── STEP 1: 기본 정보 수정
  updateProductInfoFlow(sellerToken, productId);
  sleep(0.5);

  // ── STEP 2: ON_SALE → PENDING
  const isPending = changeStatusFlow(sellerToken, productId, 'ON_SALE', 'PENDING');

  if (!isPending) {
    console.error('[productUpdateFullScenario] ON_SALE→PENDING 실패. 이후 단계를 중단합니다.');
    cleanupTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // ── STEP 3: PENDING → ON_SALE
  const isOnSale = changeStatusFlow(sellerToken, productId, 'PENDING', 'ON_SALE');

  if (!isOnSale) {
    console.error('[productUpdateFullScenario] PENDING→ON_SALE 실패. 이후 단계를 중단합니다.');
    cleanupTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // ── STEP 4: ON_SALE → DISCONTINUED
  const isDiscontinued = changeStatusFlow(sellerToken, productId, 'ON_SALE', 'DISCONTINUED');

  if (!isDiscontinued) {
    console.error('[productUpdateFullScenario] ON_SALE→DISCONTINUED 실패. 이후 단계를 중단합니다.');
    cleanupTestData(adminToken, testData);
    return;
  }

  sleep(0.5);

  // ── STEP 5: DISCONTINUED → ON_SALE (불가 전이)
  changeStatusInvalidFlow(sellerToken, productId, 'DISCONTINUED', 'ON_SALE');
  sleep(0.5);

  // ── STEP 6: DISCONTINUED → PENDING (정상 복구)
  changeStatusFlow(sellerToken, productId, 'DISCONTINUED', 'PENDING');
  sleep(0.5);

  // ── 사후 정리
  cleanupTestData(adminToken, testData);
}