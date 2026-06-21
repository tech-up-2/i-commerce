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

function sendRequest(apiName, tagName, httpMethodCall) {
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

export function paymentConfirm(authToken, paymentKey, tossOrderId, amount, options = {}) {
    const url = `${BASE_URL}/api/v1/payments/confirm`;

    const payload = JSON.stringify({
        paymentKey : paymentKey,
        tossOrderId : tossOrderId,
        amount : amount
    })

    // return sendRequest('결제 승인', () =>
    //     http.post(url, payload, getHeaders(authToken))
    // );
    const tagName = (options.tags && options.tags.name) || '결제승인';

    return sendRequest('결제 승인', tagName, () => {
        const params = getHeaders(authToken);
        params.tags = { name: tagName }; // ★ k6 메트릭 구분을 위한 태그 주입
        return http.post(url, payload, params);
    });
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