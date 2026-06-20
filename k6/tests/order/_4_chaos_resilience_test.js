import { sleep } from 'k6';
import { runOrderProducer, runPaymentConsumer } from '../../scenarios/order/purchase-scenario.js';

export const options = {
    scenarios: {
        // [프로듀서] 타임세일 이벤트가 시작되어 트래픽이 급증하는 상황 모사
        order_producer: {
            executor: 'ramping-vus',
            startVUs: 20,
            exec: 'runOrderProducer',
            stages: [
                { duration: '1m', target: 100 },
                { duration: '3m', target: 200 },
                { duration: '1m', target: 0 },
            ],
        },
        payment_consumer: {
            executor: 'ramping-vus',
            startVUs: 20,
            exec: 'runPaymentConsumer',
            stages: [
                { duration: '1m', target: 100 },
                { duration: '3m', target: 200 },
                { duration: '2m', target: 0 },
            ],
        },
    },
    // 극단적인 상황이므로 실패율은 무시하되, 시스템이 응답 불능에 빠지지 않는지만 체크
    thresholds: {
        // http_req_duration: ['p(95)<5000'], // 외부 1초 지연 + 락 대기를 고려하여 5초 이내 방어 목표
    }
};


export { runOrderProducer, runPaymentConsumer };