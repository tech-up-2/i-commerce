import { sleep } from 'k6';
import { runOrderProducer, runPaymentConsumer } from '../../scenarios/order/purchase-scenario.js';

// export const options = {
//     scenarios: {
//         // // 1. 주문을 미친듯이 생성하는 시나리오
//         order_producer: {
//             executor: 'constant-vus',
//             exec: 'runOrderProducer', // 실행할 함수명
//             vus: 50,                  // 주문 생성에 투입할 VU 수
//             duration: '5s',
//         },
//         // 2. 생성된 주문을 가져와 결제만 승인하는 시나리오
//         payment_consumer: {
//             executor: 'constant-vus',
//             exec: 'runPaymentConsumer', // 실행할 함수명
//             vus: 50,                   // 결제 승인에 투입할 VU 수
//             duration: '20s',
//         },
//     },
// };

export const options = {
    scenarios: {
        // [프로듀서] 점진적으로 주문 폭격을 증가시킴
        order_producer: {
            executor: 'ramping-vus',
            startVUs: 10,
            exec: 'runOrderProducer',
            stages: [
                { duration: '2m', target: 50 },
                { duration: '2m', target: 100 },
                { duration: '3m', target: 150 },
                { duration: '2m', target: 0 },
            ],
        },

        payment_consumer: {
            executor: 'ramping-vus',
            startVUs: 10,
            exec: 'runPaymentConsumer',
            stages: [
                { duration: '2m', target: 50 },
                { duration: '2m', target: 100 },
                { duration: '3m', target: 150 },
                { duration: '2m30s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<3000'], // 락 대기 시간 때문에 3초가 넘어가는 시점이 언제인지 파악!
    },
};


export { runOrderProducer, runPaymentConsumer };
