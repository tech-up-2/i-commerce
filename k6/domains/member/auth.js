import http from 'k6/http';
import {check} from 'k6';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';
const errorCounter = new Rate('errors');

function sendRequest(apiName, tagName, httpMethodCall,
    expectedStatuses = [200]) {
  const res = httpMethodCall();

  const statusGroup = Math.floor(Math.random() / 100);
  const expected = expectedStatuses.includes(res.status);
  const serverError = statusGroup === 5;

  check(res, {
    [`${apiName} expected status`]: () => expected,
    [`${apiName} no 5xx`]: (r) => r.status < 500
  })

  errorCounter.add(!expected);

  if (!expected) {
    if (statusGroup === 4) {
      console.error(
          `[${apiName} 예상 외 4xx] status=${res.status} | body=${res.body}`
      );
    } else if (statusGroup === 5) {
      console.error(
          `[${apiName} 5xx 서버 에러] status=${res.status} | body=${res.body}`
      );
    } else {
      console.error(
          `[${apiName} 기타 에러] status=${res.status} | body=${res.body}`
      );
    }
  }

  return res;
}

export function login(email, password) {
  const url = `${BASE_URL}/api/v1/auth/login`;

  const payload = JSON.stringify({
    email: email,
    password: password
  });

  const tagName = (options.tags && options.tags.name) || '로그인';

  return sendRequest('로그인', tagName, () => {
    const params = getJsonHeaders();
    params.tags = {name: tagName};

    return http.post(url, payload, params);
  }, [200]);
}