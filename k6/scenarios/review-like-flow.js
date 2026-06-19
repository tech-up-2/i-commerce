import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
  scenarios: {
    like_concurrency: {
      executor: 'per-vu-iterations',
      vus: 100,
      iterations: 1,
      maxDuration: '15s',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<250'],
    'http_req_failed': ['rate<0.001'],
  },
};

const TARGET_REVIEW_ID = 1;

export default function () {
  const likerId = __VU;
  const url = `http://localhost:8080/api/v1/reviews/${TARGET_REVIEW_ID}/likes?likerId=${likerId}`;
  const params = {
    headers: {
    'Content-Type': 'application/json',
    },
  };
  const res = http.post(url, {}, params);

  if (res.status !== 200) {
      console.log(`[VU: ${likerId}] ❌ 실패! 상태코드: ${res.status}, 서버 응답: ${res.body}`);
  }

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(0.1);

}