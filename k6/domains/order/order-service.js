import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function createOrder(payload, params = {}) {
  const url = `${BASE_URL}/api/orders`;
  const requestParams = Object.assign({}, params, {
    headers: Object.assign({ 'Content-Type': 'application/json' }, (params.headers || {})),
    tags: { name: 'order_create' }
  });
  const res = http.post(url, JSON.stringify(payload), requestParams);
  return res;
}

export function cancelOrder(orderId, params = {}) {
  const url = `${BASE_URL}/api/orders/${orderId}/cancel`;
  const requestParams = Object.assign({}, params, { tags: { name: 'order_cancel' } });
  return http.post(url, null, requestParams);
}

export function payOrder(orderId, paymentPayload, params = {}) {
  const url = `${BASE_URL}/api/orders/${orderId}/pay`;
  const requestParams = Object.assign({}, params, {
    headers: Object.assign({ 'Content-Type': 'application/json' }, (params.headers || {})),
    tags: { name: 'order_payment' }
  });
  const res = http.post(url, JSON.stringify(paymentPayload), requestParams);

  // 도메인 레벨에서 간단한 상태 체크는 허용(결제 성공 보장 필요)
  check(res, {
    'payment succeeded (200)': (r) => r && r.status === 200
  });

  return res;
}

