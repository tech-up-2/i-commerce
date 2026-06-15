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
    const res = httpMethodCall(); // 실제 HTTP 요청 실행

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

export function paymentConfirm(authToken, paymentKey, tossOrderId, amount) {
    const url = `${BASE_URL}/api/v1/payments/confirm`;

    const payload = JSON.stringify({
        paymentKey : paymentKey,
        tossOrderId : tossOrderId,
        amount : amount
    })

    return sendRequest('결제 승인', () =>
        http.post(url, payload, getHeaders(authToken))
    );
}

export function paymentCancel(authToken, tossOrderId, cancelAmount, paymentKey, cancelReason) {
    const url = `${BASE_URL}/api/v1/payments/cancel`;

    const payload = JSON.stringify({
        tossOrderId : tossOrderId,
        cancelAmount : cancelAmount,
        paymentKey : paymentKey,
        cancelReason : cancelReason
    })

    return sendRequest('결제 취소', () =>
        http.post(url, payload, getHeaders(authToken))
    );
}