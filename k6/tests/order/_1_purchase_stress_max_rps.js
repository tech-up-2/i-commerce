import {runPurchaseScenario} from '../../scenarios/order/purchase-scenario.js';
import {initPurchaseScenario, initTimeoutScenario, initFailureScenario} from "../../scenarios/order/wiremock-setup.js";


export function setup() {
    initPurchaseScenario();
}

export const options = {
    vus: 1,
    iterations: 1,
};

// export const options = {
//     scenarios: {
//         max_rps_test: {
//             executor: 'ramping-vus',
//             startVUs: 10,
//             stages: [
//                 { duration: '2m', target: 50 },  // 2분 동안 50명까지 상승 (웜업 단계)
//                 { duration: '2m', target: 100 }, // 2분 동안 100명까지 상승 (목표 RPS 300~500 하한선 진입)
//                 { duration: '3m', target: 150 }, // 3분 동안 150명까지 상승 (목표 VU 및 RPS 상한선 검증 구간)
//                 { duration: '2m', target: 180 }, // 2분 동안 180명까지 추가 상승 (한계점 및 2~3초 지연 시점 포착)
//                 { duration: '2m', target: 0 },   // 2분 동안 0명으로 하강하며 서버 자원 회수 확인
//             ],
//             gracefulRampDown: '30s', // 테스트 종료 시 진행 중인 주문 요청을 마무리할 유예 시간
//         },
//     },
//     // 검증 포인트(에러율 및 지연 시간) 임계치 설정
//     thresholds: {
//         http_req_failed: ['rate<0.01'],    // 에러율은 전체의 1% 미만이어야 함
//         http_req_duration: ['p(95)<3000'], // 95%의 주문 요청은 3초 이내에 완료되어야 함
//     },
// };

export default function () {
    runPurchaseScenario();
};