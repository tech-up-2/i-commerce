import {runPurchaseScenario} from '../../scenarios/order/purchase-scenario.js';
import {initFailureScenario} from "../../scenarios/order/wiremock-setup.js";


export function setup() {
    initFailureScenario();
}

export const options = {
    scenarios: {
        rollback_stress_test: {
            executor: 'constant-vus', // 200명을 고정으로 유지하여 실패 폭격 유도
            vus: 200,                 // 제안하신 스트레스 테스트 VU 수치
            duration: '3m',           // 3분 동안 지속하며 데이터 무결성 검증
        },
    },
    // [중요] 이 테스트는 80% 확률로 실패(HTTP 400 등)하는 것이 정상인 시나리오입니다.
    // 따라서 기존의 http_req_failed: ['rate<0.01'] (에러율 1% 미만) 설정을 그대로 쓰면 테스트가 무조건 Fail 납니다.
    thresholds: {
        // 80% 실패 상황이므로, 역으로 실패율이 75% ~ 85% 사이에 안착하는지 확인하는 조건입니다.
        http_req_failed: ['rate > 0.70', 'rate < 0.90'],

        // 롤백 처리가 밀리더라도 시스템이 완전히 뻗지 않고 3초 이내엔 응답을 주는지 확인합니다.
        http_req_duration: ['p(95)<3000'],
    },
};

export default function () {
    runPurchaseScenario();
};