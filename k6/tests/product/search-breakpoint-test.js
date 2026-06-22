/**
 * tests/product/search-breakpoint-test.js
 *
 * [Breakpoint Test / Stress Test] - VU를 단계적으로 올려 한계점을 탐색
 *
 * 실행: k6 run k6/tests/product/search-breakpoint-test.js \
 *         --env BASE_URL=http://localhost:8080 \
 *         --env DATA_VOLUME=<데이터 볼륨>  \
 *         --env TEST_TOKEN=<발급받은 토큰> \
 *         --out influxdb=http://influxdb:8086/k6
 */

import thresholds from '../../config/product-thresholds.js';
import { runSearchPersona } from '../../scenarios/search-personas.js';

const DATA_VOLUME = __ENV.DATA_VOLUME || 'unknown';

export const options = {
  scenarios: {
    search_breakpoint: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 50  },
        { duration: '2m', target: 50  },
        { duration: '1m', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '1m', target: 150 },
        { duration: '3m', target: 150 },
        { duration: '1m', target: 200 },
        { duration: '3m', target: 200 },
        { duration: '1m', target: 250 },
        { duration: '3m', target: 250 },
        { duration: '1m', target: 300 },
        { duration: '3m', target: 300 },
        { duration: '1m', target: 350 },
        { duration: '3m', target: 350 },
        { duration: '1m', target: 0   },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds,
  tags: {
    data_volume: DATA_VOLUME,
    test_type:   'breakpoint',
  },
};

export default function () {
  runSearchPersona();
}