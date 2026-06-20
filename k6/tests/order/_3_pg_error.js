import { sleep } from 'k6';
import { runOrderProducer, runPaymentConsumer } from '../../scenarios/order/purchase-scenario.js';

export const options = {
    scenarios: {
        // [프로듀서] 에러 상황에서 DB에 얼마나 부하가 가는지 보기 위해 100명 투입
        order_producer: {
            executor: 'constant-vus',
            vus: 100,
            duration: '3m',
            exec: 'runOrderProducer',
        },
        // [컨슈머] 무자비한 실패 폭격을 유도. 에러 처리(Exception) 과정의 CPU 오버헤드 확인
        payment_consumer: {
            executor: 'constant-vus',
            vus: 200, // 컨슈머 VU를 확 높여서 동시다발적인 에러 핸들링 유도
            duration: '3m30s',
            exec: 'runPaymentConsumer',
        },
    },
    thresholds: {
        // 의도적인 에러 테스트이므로 http_req_failed 검증은 주석 처리하거나 제외합니다.
    }
};


export { runOrderProducer, runPaymentConsumer };