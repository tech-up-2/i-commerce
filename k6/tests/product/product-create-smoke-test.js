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

import { getAuthTokenFromEnv, getAdminToken } from '../../lib/auth-helper.js';
import { getStoreId } from '../../lib/store-helper.js';
import { productCreateFullScenario } from '../../scenarios/product-create-flows.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const sellerToken = getAuthTokenFromEnv();
  const adminToken = getAdminToken();
  const storeId = getStoreId();

  if (!sellerToken || !adminToken || !storeId) {
    console.error('[Smoke] 필수 환경변수 누락');
    return;
  }

  productCreateFullScenario(adminToken, sellerToken, storeId);
}