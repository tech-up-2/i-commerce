/**
 * Review Smoke Test
 *
 * 실행 명령어:
 * k6 run k6/tests/review/review-smoke-test.js --env TEST_TOKEN=... --env ADMIN_TOKEN=... --env VIEWER_TOKEN=...
 */
//
import { flow } from '../../scenarios/review-flows.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  if (!__ENV.TEST_TOKEN || !__ENV.ADMIN_TOKEN || !__ENV.VIEWER_TOKEN) {
    console.error('[Smoke] 필수 토큰(TEST, ADMIN, VIEWER) 중 누락된 것이 있습니다.');
    return;
  }

  flow();
}