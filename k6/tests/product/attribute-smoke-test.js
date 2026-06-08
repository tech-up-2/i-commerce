/**
 * Attribute Smoke Test
 *
 * 실행 명령어:
 *   k6 run k6/tests/attribute/attribute-smoke-test.js --env TEST_TOKEN=
 *
 */
import { getAuthTokenFromEnv } from '../../lib/auth-helper.js';
import { attributeFullScenario } from '../../scenarios/attribute-flows.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  if (!getAuthTokenFromEnv()) {
    console.error('[Smoke] TEST_TOKEN 없음.');
    return;
  }
  attributeFullScenario();
}