import { sleep } from 'k6';
import { runOrderProducer, runPaymentConsumer } from '../../scenarios/order/purchase-scenario.js';

export const options = {
    scenarios: {
        // [프로듀서] 꾸준한 속도로 지속적인 주문 생성 (자원 고갈을 유도하기 위한 베이스라인)
        order_producer: {
            executor: 'constant-vus',
            vus: 50,
            duration: '3m',
            exec: 'runOrderProducer',
        },
        // [컨슈머] 결제 지연으로 인해 컨슈머가 멈춰있게 됨. VU를 100명으로 올려서 스레드 고갈 유도
        payment_consumer: {
            executor: 'constant-vus',
            vus: 100,
            duration: '3m30s', // 프로듀서 종료 후 남은 큐 처리
            exec: 'runPaymentConsumer',
        },
    },
    // 지연 테스트이므로 duration 제한은 없애거나 넉넉하게 둡니다.
};


export { runOrderProducer, runPaymentConsumer };