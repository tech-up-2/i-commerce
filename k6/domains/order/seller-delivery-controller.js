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
    const res = httpMethodCall(); // 실제 HTTP 요청 실행

    // 1. 공통 검증 로직 (2xx 성공 여부)
    const isOk = check(res, {
        [`${apiName} 성공 (2xx)`]: (r) => Math.floor(r.status / 100) === 2,
    });

    // 2. 공통 실패 로그 로직
    if (!isOk) {
        errorCounter.add(true);
        console.error(`[${apiName} 실패] 상태코드: ${res.status} | 원인: ${res.body}`);
    }

    return res;
}

export function getDeliveryList(authToken, storeId, trackingNumber) {
    const url = `${BASE_URL}/api/v1/deliveries/${storeId}?status=preparing`;

    return sendRequest('배송 목록 조회', () =>
        http.get(url, getHeaders(authToken))
    );
}

export function updateDeliveryStatus(authToken, deliverId, trackingNumber) {
    const url = `${BASE_URL}/api/v1/deliveries/ship`;

    const payload = JSON.stringify({
        deliverId: deliverId,
        trackingNumber: trackingNumber
    });

    return sendRequest('배송 상태 업데이트', () =>
        http.patch(url, payload, getHeaders(authToken))
    );
}