import http from 'k6/http';
import {check} from 'k6';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export const options = {
  vus: 1,
  duration: '30s',
};

export default function () {
  const url = `${BASE_URL}/api/v1/auth/login`;

  const payload = JSON.stringify({
    email: 'activeMember1@test.com',
    password: 'password123!',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: {
      name: '로그인',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    '로그인 성공': (r) => r.status === 200,
    '서버 에러 없음': (r) => r.status < 500,
  });
}