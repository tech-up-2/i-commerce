import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function getHeaders(authToken) {
  return {
    headers : {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
    },
  };
}

function sendRequest(apiName, httpMethodCall) {
  const res = httpMethodCall();

  // 1. 공통 검증 로직 (2xx 성공 여부)
  const isOk = check(res, {
    [`${apiName} 성공 (2xx)`]: (r) => Math.floor(r.status / 100) === 2,
  });

  // 2. 공통 실패 로그 로직
  if (!isOk) {
    console.error(`[${apiName} 실패] 상태코드: ${res.status} | 원인: ${res.body}`);
  }

  return res;
}

export function createOrder(authToken, addressId, items) {
  const url = `${BASE_URL}/api/v1/orders`;

  const payload = JSON.stringify({
    addressId: addressId,
    items: items
  });

  const res = http.post(url, payload, getHeaders(authToken));

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

