import { check, sleep } from 'k6';

import { createOrder, getOrderDetail } from '../domains/order/order-controller.js';
import { paymentConfirm } from '../domains/order/payment-controller.js'


const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function runPurchaseScenario() {
    const jsonHeaders = { headers: { 'Content-Type': 'application/json' } };

    // 1. 로그인


    // 2. 상품 상세 조회


    // 3. 주문 생성
    const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaXNzIjoiaS1jb21tZXJjZSIsInByaW5jaXBhbFR5cGUiOiJNRU1CRVIiLCJhY2NvdW50SWQiOjEsInJvbGUiOiJDVVNUT01FUiIsImFjY291bnRTdGF0dXMiOiJBQ1RJVkUiLCJpYXQiOjE3ODA1NTY1ODQsImV4cCI6MTc4MDU2MDE4NH0.WknCiw1-kGarCty__l7TmU4okBfsiIqXNeg3k1iFRJY';
    const mockItems = [{ productId: 1, quantity: 1 }];

    const orderRes = createOrder(token, 1, mockItems);
    console.log(`================주문 생성 결과: ${orderRes ? '성공' : '실패'}`);
    if (!orderRes) return;
    sleep(1);


    const responseBody = orderRes.json();
    // 4. 최종 결제 승인

    const uniqueId = Math.random().toString(36).substring(2, 10);
    const fakePaymentKey = `pay_${uniqueId}`;
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;

    console.log(`================tossOrderId: ${tossOrderId}`);


    const confirmRes = paymentConfirm(token, fakePaymentKey, tossOrderId, amount);

    check(confirmRes, { '4. 최종 결제 승인 성공': (r) => r.status === 200 });

    sleep(2);

}