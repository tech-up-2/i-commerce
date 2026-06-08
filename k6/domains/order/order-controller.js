import http from 'k6/http';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function getHeaders(authToken) {
  return {
    headers : {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
    },
  };
}

export function createOrder(authToken, addressId, items) {
  const url = `${BASE_URL}/api/v1/orders`;

  const payload = JSON.stringify({
    addressId: addressId,
    items: items
  });

  return http.post(url, payload, getHeaders(authToken));
}

export function getOrderList(authToken) {
  const url = `${BASE_URL}/api/v1/orders`;

  return http.get(url, getHeaders(authToken));
}

export function getOrderDetail(authToken, orderId) {
  const url = `${BASE_URL}/api/v1/orders/${orderId}`;

  return http.get(url, getHeaders(authToken));
}

