import { sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import exec from 'k6/execution';
import { createOrder } from '../../domains/order/order-controller.js';

const csvData = new SharedArray('member_tokens_6', function () {
    return papaparse.parse(open('./users.csv'), { header: true }).data;
});

export function runAbortionUserScenario() {
    const userIndex = exec.scenario.iterationInTest % csvData.length;
    const token = csvData[userIndex].token;

    // 이탈 유저 폭주: 매번 다른 상품 조합으로 주문서만 쓰고 결제 안 하고 튕김
    const mockItems = [
        {
            productId: Math.floor(Math.random() * 50) + 1,
            quantity: Math.floor(Math.random() * 3) + 1
        }
    ];
    const addressId = 1;

    // 오직 주문서 생성(Insert)만 무한 반복 유발
    createOrder(token, addressId, mockItems);

    sleep(0.5); // 빠르게 주문서만 생성하도록 대기시간 최소화
}
