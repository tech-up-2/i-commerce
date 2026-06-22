/**
 * tests/product/search-load-test.js
 *
 * [Load Test] - 특정 VU 수준에서 일정 시간 유지하며 안정성 검증
 *
 * 실행: k6 run k6/tests/product/search-load-test.js \
 *         --env BASE_URL=http://localhost:8080 \
 *         --env TARGET_VUS=<검증할 특정 VU 수준> \
 *         --env DATA_VOLUME=<데이터 볼륨> \
 *         --env TEST_TOKEN=<발급받은 토큰> \
 *         --out influxdb=http://influxdb:8086/k6
 */

import thresholds from '../../config/product-thresholds.js';
import { runSearchPersona } from '../../scenarios/search-personas.js';

const TARGET_VUS  = parseInt(__ENV.TARGET_VUS  || '500');
const DATA_VOLUME = __ENV.DATA_VOLUME || 'unknown';

export const options = {
  scenarios: {
    search_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m',  target: Math.round(TARGET_VUS * 0.3) },
        { duration: '1m',  target: TARGET_VUS },
        { duration: '5m',  target: TARGET_VUS },
        { duration: '1m',  target: 0          },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds,
  tags: {
    data_volume: DATA_VOLUME,
    test_type:   'load',
  },
};

export default function () {
  runSearchPersona();
}