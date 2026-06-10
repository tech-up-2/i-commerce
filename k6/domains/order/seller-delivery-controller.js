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

export function getDeliveryList(authToken, storeId, trackingNumber) {
    const url = `${BASE_URL}/api/v1/deliveries/${storeId}?status=preparing`;

    return http.patch(url, getHeaders(authToken));
}

export function updateDeliveryStatus(authToken, deliverId, trackingNumber) {
    const url = `${BASE_URL}/api/v1/deliveries/ship`;

    const payload = JSON.stringify({
        deliverId: deliverId,
        trackingNumber: trackingNumber
    });

    return http.patch(url, payload, getHeaders(authToken));
}