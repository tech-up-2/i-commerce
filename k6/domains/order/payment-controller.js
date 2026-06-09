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

export function paymentConfirm(authToken, paymentKey, tossOrderId, amount) {
    const url = `${BASE_URL}/api/v1/payments/confirm`;

    const payload = JSON.stringify({
        paymentKey : paymentKey,
        tossOrderId : tossOrderId,
        amount : amount
    })

    return http.post(url, payload, getHeaders(authToken));
}

export function paymentCancel(authToken, tossOrderId, cancelAmount, paymentKey, cancelReason) {
    const url = `${BASE_URL}/api/v1/payments/cancel`;

    const payload = JSON.stringify({
        tossOrderId : tossOrderId,
        cancelAmount : cancelAmount,
        paymentKey : paymentKey,
        cancelReason : cancelReason
    })

    return http.post(url, payload, getHeaders(authToken));
}