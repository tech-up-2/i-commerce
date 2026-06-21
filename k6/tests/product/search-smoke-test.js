/**
 * k6/tests/product/searsh-smoke-test.js
 *
 * Smoke Test
 *
 * 실행:
 *   k6 run k6/tests/01-smoke-test.js --env TEST_TOKEN=<토큰> --env DATA_VOLUME=<> \
 *     --env BASE_URL=http://localhost:8080 \
 *     --env LEAF_CATEGORY_IDS=<실제ID목록>
 *
 */
import { runSearchPersona } from '../../scenarios/search-personas.js';

const DATA_VOLUME = __ENV.DATA_VOLUME || 'unknown';

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus:      2,
      duration: '2m',
    },
  },
  thresholds: {
    'http_req_failed': [{ threshold: 'rate<0.01', abortOnFail: true }],
    'http_req_duration{name:product_search}': [
      { threshold: 'p(95)<3000', abortOnFail: true },
    ],
  },
  tags: {
    data_volume: DATA_VOLUME,
    test_stage: 'smoke'
  },
};

export default function () {
  runSearchPersona();
}