import {runDoubleSubmitScenario} from "../../scenarios/order/_5_payment_double_submit_scenario.js";
import {initPurchaseScenario} from "../../scenarios/order/wiremock-setup.js";


export function setup() {
    initPurchaseScenario();
}

export const options = {
    scenarios: {
        double_submit_test: {
            executor: 'per-vu-iterations', // 각 VU가 정해진 횟수(1번)만큼 시나리오 실행
            vus: 50,                       // 동시에 '따닥'을 누를 가상 사용자 50명
            iterations: 1,                 // 유저당 이 시나리오(runDoubleSubmitScenario)를 1번만 실행
            maxDuration: '1m',             // 테스트 최대 제한 시간 1분
        },
    },
    thresholds: {
        // 50명이 각각 10번(총 500번)의 연타를 칩니다.
        // 그중 50번만 성공(200)하고 450번은 실패(400, 409 등)해야 하므로 에러율은 90% 근처여야 합니다.
        // (단, 주문 생성 API 성공률은 제외하고 결제 API 에러율만 정확히 보려면 태그(Tag) 구분이 필요하나,
        //  전체 에러율로 가늠할 때는 약 80%~85% 이상 실패가 뜨면 정상입니다.)
        http_req_failed: ['rate > 0.80'],
    },
};


export default function () {
    runDoubleSubmitScenario();
};