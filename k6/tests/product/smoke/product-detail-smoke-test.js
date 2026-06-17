/**
 * Product Detail Smoke Test
 *
 * 실행 명령어:
 *   k6 run k6/tests/product/product-detail-smoke-test.js
 *     --env ADMIN_TOKEN=... \
 *     --env TEST_TOKEN=... \
 *     --env TEST_STORE_ID=
 *
 */
import { getAuthTokenFromEnv, getAdminToken } from '../../../lib/auth-helper.js';
import { getStoreId } from '../../../lib/store-helper.js';
import { productDetailFullScenario } from '../../../scenarios/product_api/product-detail-flows.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const sellerToken = getAuthTokenFromEnv();
  const adminToken = getAdminToken();
  const storeId = getStoreId();

  if (!sellerToken || !adminToken || !storeId) {
    console.error('[Smoke] 필수 환경변수가 없습니다.');
    return;
  }

  productDetailFullScenario(adminToken, sellerToken, storeId);
}