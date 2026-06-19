import { sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import exec from 'k6/execution';
import { createOrder } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js';

const csvData = new SharedArray('member_tokens', function () {
    const fileData = open('../data/dummy-tokens.csv');
    return papaparse.parse(fileData, { header: true }).data;
});

export function runTimeSaleSpikeScenario() {
    const userIndex = exec.scenario.iterationInTest % csvData.length;
    const token = csvData[userIndex].token;

    // 타임세일: 상품 ID 1번, 수량 1개로 전 유저 고정
    const mockItems = [{ productId: 1, quantity: 1 }];
    const addressId = 1;

    // 1. 주문 생성
    const orderRes = createOrder(token, addressId, mockItems);
    if (Math.floor(orderRes.status / 100) !== 2) return; // 락 병목으로 실패 시 즉시 종료

    const responseBody = orderRes.json();
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;
    const fakePaymentKey = `pay_spike_${Math.random().toString(36).substring(2, 10)}`;

    // 2. 최종 결제 승인 (동시성 락 경합 발생 지점)
    paymentConfirm(token, fakePaymentKey, tossOrderId, amount);
}