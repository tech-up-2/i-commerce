import {runPurchaseScenario} from '../../scenarios/order/purchase-scenario.js';
import {initPurchaseScenario, initTimeoutScenario, initFailureScenario} from "../../scenarios/order/wiremock-setup.js";


export function setup() {
    initPurchaseScenario();
}

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    runPurchaseScenario();
};