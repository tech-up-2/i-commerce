import {runPurchaseScenario} from '../../scenarios/order/purchase-scenario.js';
import {initTimeoutScenario} from "../../scenarios/order/wiremock-setup.js";


export function setup() {
    initTimeoutScenario();
}


export const options = {
    scenarios: {
        pg_delay_test: {
            executor: 'constant-vus', // 100명을 고정으로 유지하여 자원 고갈 유도
            vus: 100,                 // 제안하신 타깃 VU 100명 설정
            duration: '3m',           // 3분 동안 지속하며 커넥션 누수나 행업 현상 관찰
        },
    },
    // 이 시나리오의 핵심은 '3초 타임아웃 에러'가 정상적으로 터지는지 확인하는 것입니다.
    thresholds: {
        // [중요] 5초 지연이 발생하므로, 3초 타임아웃이 걸리면 100% 에러가 발생합니다.
        // 따라서 에러율 임계치를 넣지 않거나, 역으로 에러가 발생하는지 모니터링해야 합니다.
        // 여기서는 에러율 대신, 타임아웃이 작동해 응답 시간 상한선이 3.5초 이내로 끊기는지 검증합니다.
        http_req_duration: ['p(99)<3500'], // 타임아웃(3초) + 네트워크 마진을 고려해 3.5초 이내에 끊겨야 함
    },
};

export default function () {
    runPurchaseScenario();
};