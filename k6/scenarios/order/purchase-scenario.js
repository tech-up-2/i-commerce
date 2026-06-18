import { check, sleep } from 'k6';
import { createOrder, getOrderDetail } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js'
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

// const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

const csvData = new SharedArray('member_tokens', function () {
    const fileData = open('../../data/dummy-tokens.csv');
    return papaparse.parse(fileData, { header: true }).data;
});

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function runPurchaseScenario() {
    const jsonHeaders = { headers: { 'Content-Type': 'application/json' } };

    // const userIndex = exec.scenario.iterationInTest % csvData.length;
    const userIndex = getRandomInt(0, 14999);
    const currentUser = csvData[userIndex];

    if (!currentUser || !currentUser.token) {
        console.error(`================[Error] ${userIndex}번 인덱스에서 토큰을 찾지 못했습니다.`);
        return;
    }

    const token = `${currentUser.token}`;

    let addressId = null;
    if (currentUser.deliveryAddressIds) {
        const addressIds = currentUser.deliveryAddressIds.split('|');
        addressId = addressIds[0]; // 첫 번째 주소 사용 (필요시 이 안에서도 랜덤 선택 가능)
    }

    // 주소 값이 없을 경우를 대비한 예외 처리
    if (!addressId) {
        console.error(`================[Error] ${userIndex}번 유저의 배송지 주소가 없습니다.`);
        return;
    }

    const itemsCount = getRandomInt(1, 3);
    const mockItems = [];
    for (let i = 0; i < itemsCount; i++) {
        mockItems.push({
            productId: getRandomInt(1, 50),
            quantity: getRandomInt(1, 5),
        });
    }

    const orderRes = createOrder(token, addressId, mockItems, { tags: { name: '주문생성' } });
    if (!orderRes) return;
    // console.log(`================주문 생성 결과: ${orderRes ? '성공' : '실패'}`);
    sleep(1);

    let responseBody;
    if (orderRes.body && orderRes.body.trim().length > 0) {
        responseBody = orderRes.json();
    } else {
        console.log(`[경고] 응답 바디가 비어있습니다. 상태 코드: ${orderRes.status}`);
    }

    const uniqueId = Math.random().toString(36).substring(2, 10);
    const fakePaymentKey = `pay_${uniqueId}`;
    const tossOrderId = responseBody.data.tossOrderId;
    const amount = responseBody.data.amount;

    // console.log(`================tossOrderId: ${tossOrderId}`);

    const confirmRes = paymentConfirm(token, fakePaymentKey, tossOrderId, amount, { tags: { name: '결제승인' } });

    // check(confirmRes, { '최종 결제 승인 성공': (r) => r.status === 200 });
    // 3번
    // check(confirmRes, {
    //     '타임아웃 안전장치 작동 (5초 다 안 걸리고 3초대 차단)': (r) => r.timings.duration < 3500,
    // });
    sleep(2);
}