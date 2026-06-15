import { runPurchaseScenario } from '../../scenarios/order/_1_purchase-scenario.js';

// export const options = {
//     // 딱 1명의 가상 유저(VU)가 1번만 실행하고 종료하도록 설정 (동작 확인용)
//     vus: 1,
//     iterations: 1,
// };

export const options = {
    scenarios: {
        max_rps_test: {
            executor: 'ramping-vus', // 계단식 VU 조절을 위한 익제큐터
            startVUs: 10,            // 처음 시작할 가상 유저 수
            stages: [
                { duration: '1m', target: 50 },  // 1분 동안 50명까지 서서히 상승
                { duration: '2m', target: 100 }, // 2분 동안 100명까지 서서히 상승
                { duration: '2m', target: 300 }, // 2분 동안 300명까지 서서히 상승
                { duration: '3m', target: 500 }, // 3분 동안 500명까지 서서히 상승 (피크 스펙 측정)
                { duration: '2m', target: 0 },   // 2분 동안 0명으로 하강하며 자원 회수 확인
            ],
            gracefulRampDown: '30s', // 테스트 종료 시 진행 중인 요청을 마무리할 유예 시간
        },
    },
    // 검증 포인트(지연 시간)를 k6 결과창에서 바로 확인하기 위한 임계치 설정
    thresholds: {
        http_req_failed: ['rate<0.01'],    // 에러율 1% 미만 유지 조건
        http_req_duration: ['p(95)<3000'], // 95%의 요청은 3초 이내에 완료되어야 함 (2~3초 지연 확인용)
    },
};

export default function () {
    runPurchaseScenario();
};