/**
 * Smoke Test
 *
 * 실행 명령어:
 *   k6 run k6/tests/smoke-test.js --env TEST_TOKEN=eyJhbGciOi...
 */
import { getAuthTokenFromEnv } from '../lib/auth-helper.js';
import { categoryFullScenario } from '../scenarios/product_api/category-flows.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  if (!getAuthTokenFromEnv()) {
    console.error('TEST_TOKEN 없음.');
    return;
  }
  
  categoryFullScenario();
}