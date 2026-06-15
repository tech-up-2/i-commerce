import { sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import exec from 'k6/execution';
import { createOrder } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js';

const csvData = new SharedArray('member_tokens_5', function () {
    return papaparse.parse(open('./users.csv'), { header: true }).data;
});

export function runDoubleSubmitScenario() {
    const userIndex = exec.scenario.iterationInTest % csvData.length;
    const token = csvData[userIndex].token;

    const mockItems = [{ productId: Math.floor(Math.random() * 50) + 1, quantity: 1 }];
    const addressId = 1;

    // 1. 정상적으로 주문을 딱 1번 만듭니다.
    const orderRes = createOrder(token, addressId, mockItems);
    if (Math.floor(orderRes.status / 100) !== 2) return;

    const responseBody = orderRes.json();
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;
    const fakePaymentKey = `pay_double_${Math.random().toString(36).substring(2, 10)}`;

    // 2. ⚡ 한국인 특유의 10번 연속 "따닥" 연타 폭격 시뮬레이션
    // 루프 사이에 sleep을 주지 않거나 극소량(0.01초)만 주어 순식간에 10번 쏩니다.
    for (let i = 0; i < 10; i++) {
        paymentConfirm(token, fakePaymentKey, tossOrderId, amount);
        sleep(0.01);
    }

    sleep(2);
}