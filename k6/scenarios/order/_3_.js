import { sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import exec from 'k6/execution';
import { createOrder } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js';

const csvData = new SharedArray('member_tokens_3', function () {
    return papaparse.parse(open('./users.csv'), { header: true }).data;
});

export function runPgDelayScenario() {
    const userIndex = exec.scenario.iterationInTest % csvData.length;
    const token = csvData[userIndex].token;

    const mockItems = [{ productId: Math.floor(Math.random() * 50) + 1, quantity: 1 }];
    const addressId = 1;

    // 1. 주문 생성
    const orderRes = createOrder(token, addressId, mockItems);
    if (Math.floor(orderRes.status / 100) !== 2) return;

    const responseBody = orderRes.json();
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;
    const fakePaymentKey = `pay_delay_${Math.random().toString(36).substring(2, 10)}`;

    // 2. 최종 결제 승인 (이 API 호출이 5초간 대기 상태에 빠질 것임)
    paymentConfirm(token, fakePaymentKey, tossOrderId, amount);

    sleep(1);
}