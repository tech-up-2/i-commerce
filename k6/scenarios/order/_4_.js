import { sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import exec from 'k6/execution';
import { createOrder } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js';

const csvData = new SharedArray('member_tokens_4', function () {
    return papaparse.parse(open('./users.csv'), { header: true }).data;
});

export function runRollbackStressScenario() {
    const userIndex = exec.scenario.iterationInTest % csvData.length;
    const token = csvData[userIndex].token;

    const mockItems = [{ productId: Math.floor(Math.random() * 50) + 1, quantity: 1 }];
    const addressId = 1;

    // 1. 주문 생성 (재고 선점)
    const orderRes = createOrder(token, addressId, mockItems);
    if (Math.floor(orderRes.status / 100) !== 2) return;

    const responseBody = orderRes.json();
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;
    const fakePaymentKey = `pay_fail_${Math.random().toString(36).substring(2, 10)}`;

    // 2. 최종 결제 승인 요청 (Mock 서버의 80% 확률 에러 발생 구간 -> 스프링의 @Transactional 롤백 자극)
    paymentConfirm(token, fakePaymentKey, tossOrderId, amount);

    sleep(1);
}