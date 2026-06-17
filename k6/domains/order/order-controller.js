import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function getHeaders(authToken) {
  return {
    headers : {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
    },
  };
}

const errorCounter = new Rate('errors');

function sendRequest(apiName, httpMethodCall) {
  const res = httpMethodCall();

  // 1. 공통 검증 로직 (성공, 4xx, 5xx 구분)
  const checkResult = check(res, {
    [`${apiName} 성공 (2xx)`]: (r) => Math.floor(r.status / 100) === 2,
    [`${apiName} 클라이언트 에러 (4xx)`]: (r) => Math.floor(r.status / 100) === 4,
    [`${apiName} 서버 에러 (5xx)`]: (r) => Math.floor(r.status / 100) === 5,
  });

  // 2. 실패 로그 로직 분기
  const statusGroup = Math.floor(res.status / 100);

  if (statusGroup !== 2) {
    errorCounter.add(true); // 기존 에러 카운터 유지

    if (statusGroup === 4) {
      console.error(`[${apiName} 4xx 에러] 상태코드: ${res.status} | 원인: ${res.body}`);
    } else if (statusGroup === 5) {
      console.error(`[${apiName} 5xx 에러] 상태코드: ${res.status} | 원인: ${res.body}`);
    } else {
      console.error(`[${apiName} 기타 에러] 상태코드: ${res.status} | 원인: ${res.body}`);
    }
  }

  return res;
}

export function createOrder(authToken, addressId, items) {
  const url = `${BASE_URL}/api/v1/orders`;

  const payload = JSON.stringify({
    addressId: addressId,
    items: items
  });

  return sendRequest('주문 생성', () =>
      http.post(url, payload, getHeaders(authToken))
  );
}

export function getOrderList(authToken) {
  const url = `${BASE_URL}/api/v1/orders`;

  return sendRequest('주문 목록 조회', () =>
      http.get(url, getHeaders(authToken))
  );
}

export function getOrderDetail(authToken, orderId) {
  const url = `${BASE_URL}/api/v1/orders/${orderId}`;

  return sendRequest('주문 상세 조회', () =>
      http.get(url, getHeaders(authToken))
  );
}

