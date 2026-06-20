import { check, sleep } from 'k6';
import { createOrder, getOrderDetail } from '../../domains/order/order-controller.js';
import { paymentConfirm } from '../../domains/order/payment-controller.js'
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import redis from 'k6/x/redis';

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
            productId: getRandomInt(24001, 513430),
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


export async function runOrderProducer() {
    const redisClient = new redis.Client('redis://127.0.0.1:6379');
    const userIndex = getRandomInt(0, 14999);
    const currentUser = csvData[userIndex];

    // const delay = getRandomInt(2000, 5000);
    const delay = 0;
    const executeAt = Date.now() + delay;

    if (!currentUser || !currentUser.token || !currentUser.deliveryAddressIds) {
        return;
    }

    const token = `${currentUser.token}`;
    const addressId = currentUser.deliveryAddressIds.split('|')[0];

    const itemsCount = getRandomInt(1, 3);
    const mockItems = [];
    for (let i = 0; i < itemsCount; i++) {
        mockItems.push({
            productId: getRandomInt(1, 480000),
            quantity: getRandomInt(1, 5),
        });
    }

    // [요청 1] 주문 생성
    const orderRes = createOrder(token, addressId, mockItems, { tags: { name: '주문생성' } });

    if (orderRes && orderRes.status === 200 && orderRes.body.trim().length > 0) {
        const responseBody = orderRes.json();

        // Redis에 저장할 결제용 데이터 객체 생성
        const paymentTask = {
            token: token,
            tossOrderId: responseBody.data.tossOrderId,
            amount: responseBody.data.amount
        };

        const isPaymentCompleted = Math.random() < 0.7;

        if (isPaymentCompleted) {
            await redisClient.sendCommand('ZADD', 'order_zset', String(executeAt), JSON.stringify(paymentTask));
        }
    }
}


export async function runPaymentConsumer() {
    const redisClient = new redis.Client('redis://127.0.0.1:6379');
    const now = Date.now();

    const visualRange = await redisClient.sendCommand('ZRANGEBYSCORE', 'order_zset', '0', String(now), 'LIMIT', '0', '1');

    if (!visualRange || visualRange.length === 0) {
        sleep(0.2);
        return;
    }

    const rawData = visualRange[0];

    const removedCount = await redisClient.sendCommand('ZREM', 'order_zset', rawData);
    if (removedCount !== 1) {
        return;
    }

    const paymentData = JSON.parse(rawData);

    const uniqueId = Math.random().toString(36).substring(2, 10);
    const fakePaymentKey = `pay_${uniqueId}`;

    // [요청 2] 결제 승인 API 호출 (유저의 카드 인증 시간이 지난 것처럼 비동기 처리됨)
    const confirmRes = paymentConfirm(
        paymentData.token,
        fakePaymentKey,
        paymentData.tossOrderId,
        paymentData.amount,
        { tags: { name: '결제승인' } }
    );

    // 실제 유저의 처리 속도나 네트워크 지연을 약간 시뮬레이션하고 싶다면
    // 결제 성공/실패 처리 후 아주 짧은 sleep을 줄 수 있습니다.
    sleep(0.5);
}
