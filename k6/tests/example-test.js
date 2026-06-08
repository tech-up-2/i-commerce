import { runPurchaseScenario } from '../scenarios/purchase-scenario.js';

export const options = {
    // 딱 1명의 가상 유저(VU)가 1번만 실행하고 종료하도록 설정 (동작 확인용)
    vus: 1,
    iterations: 1,
};

export default function () {
    runPurchaseScenario();
};