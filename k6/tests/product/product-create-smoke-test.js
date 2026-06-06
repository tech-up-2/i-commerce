/**
 * Product Create Smoke Test
 *
 * 토큰 역할:
 *   ADMIN_TOKEN  → 사전 준비 (카테고리/옵션/속성 생성 및 연결)
 *   TEST_TOKEN   → 상품 생성
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/product-create-smoke-test.js \
 *     --env ADMIN_TOKEN=... \
 *     --env TEST_TOKEN=... \
 *     --env TEST_STORE_ID=
 *
 */
import { sleep } from 'k6';
import { getAuthTokenFromEnv, getAdminToken } from '../../lib/auth-helper.js';
import { getStoreId } from '../../lib/store-helper.js';
import {
  setupProductTestData,
  cleanupProductTestData,
  createProductNoneFlow,
  createProductSingleFlow,
  createProductDoubleFlow,
} from '../../scenarios/product-create-flows.js';

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

  // 사전 준비
  console.log('[Smoke] 사전 준비 시작 (관리자 토큰)');

  const testData = setupProductTestData(adminToken);

  if (!testData) {
    console.error('[Smoke] 사전 준비 실패. 테스트를 중단합니다.');
    return;
  }

  console.log('[Smoke] 사전 준비 완료');
  sleep(0.5);

  // CASE 1: NONE 타입

  console.log('\n[Smoke] ===== 케이스 1: NONE 타입 =====');
  createProductNoneFlow(sellerToken, storeId, testData);
  sleep(0.5);

  // CASE 2: SINGLE 타입

  console.log('\n[Smoke] ===== 케이스 2: SINGLE 타입 =====');
  createProductSingleFlow(sellerToken, storeId, testData);
  sleep(0.5);

  // CASE 3: DOUBLE 타입

  console.log('\n[Smoke] ===== 케이스 3: DOUBLE 타입 =====');
  createProductDoubleFlow(sellerToken, storeId, testData);
  sleep(0.5);

  // 사후 정리

  console.log('\n[Smoke] 사후 정리 시작 (관리자 토큰)');
  cleanupProductTestData(adminToken, testData);

  console.log('[Smoke] 완료');
}