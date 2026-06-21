import {check} from 'k6';
import {Rate} from 'k6/metrics';

const errorCounter = new Rate('errors');

export function sendRequest(apiName, httpMethodCall, expectedStatuses = [200]) {
  const res = httpMethodCall();

  const statusGroup = Math.floor(res.status / 100);
  const expected = expectedStatuses.includes(res.status);

  check(res, {
    [`${apiName} expected status`]: () => expected,
    [`${apiName} no 5xx`]: () => res.status < 500,
  });

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