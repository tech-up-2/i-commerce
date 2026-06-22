/**
 * Product Create Scenarios
 *
 * payload 생성: lib/product-payload-builder.js
 * 사전 준비/정리: lib/product-helper.js
 */
import { check, sleep } from 'k6';
import { createProduct } from '../../domains/product/product-command-service.js';
import { setupTestData, cleanupTestData } from '../../lib/product-helper.js';
import {
  buildNoneProductPayload,
  buildSingleProductPayload,
  buildDoubleProductPayload,
} from '../../lib/product-payload-builder.js';

/**
 * [케이스 1] NONE 타입 상품 생성
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductNoneFlow(sellerToken, storeId, testData) {
  const res = createProduct(sellerToken, buildNoneProductPayload(storeId, testData));

  const isSuccess = check(res, {
    '[createProduct/NONE] status is 200': (r) => r.status === 200,
    '[createProduct/NONE] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[createProduct/NONE] 응답에 productId 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.productId === 'number'; } catch { return false; }
    },
  });

  if (!isSuccess) {
    console.error(`[createProductNoneFlow] 실패 | status: ${res.status} | body: ${res.body}`);
    return null;
  }

  return JSON.parse(res.body).data.productId;
}

/**
 * [케이스 2] SINGLE 타입 상품 생성
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductSingleFlow(sellerToken, storeId, testData) {
  const res = createProduct(sellerToken, buildSingleProductPayload(storeId, testData));

  const isSuccess = check(res, {
    '[createProduct/SINGLE] status is 200': (r) => r.status === 200,
    '[createProduct/SINGLE] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[createProduct/SINGLE] 응답에 productId 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.productId === 'number'; } catch { return false; }
    },
  });

  if (!isSuccess) {
    console.error(`[createProductSingleFlow] 실패 | status: ${res.status} | body: ${res.body}`);
    return null;
  }

  return JSON.parse(res.body).data.productId;
}

/**
 * [케이스 3] DOUBLE 타입 상품 생성
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductDoubleFlow(sellerToken, storeId, testData) {
  const res = createProduct(sellerToken, buildDoubleProductPayload(storeId, testData));

  const isSuccess = check(res, {
    '[createProduct/DOUBLE] status is 200': (r) => r.status === 200,
    '[createProduct/DOUBLE] code is SUCCESS': (r) => {
      try { return JSON.parse(r.body).code === 'SUCCESS'; } catch { return false; }
    },
    '[createProduct/DOUBLE] 응답에 productId 존재': (r) => {
      try { return typeof JSON.parse(r.body).data.productId === 'number'; } catch { return false; }
    },
  });

  if (!isSuccess) {
    console.error(`[createProductDoubleFlow] 실패 | status: ${res.status} | body: ${res.body}`);
    return null;
  }

  return JSON.parse(res.body).data.productId;
}

/**
 * 상품 생성 통합 시나리오
 * NONE / SINGLE / DOUBLE 3가지 타입 생성을 순서대로 실행합니다.
 *
 * @param {string} adminToken
 * @param {string} sellerToken
 * @param {number} storeId
 */
export function productCreateFullScenario(adminToken, sellerToken, storeId) {
  const testData = setupTestData(adminToken);

  if (!testData) return;

  sleep(0.5);

  createProductNoneFlow(sellerToken, storeId, testData);
  sleep(0.5);

  createProductSingleFlow(sellerToken, storeId, testData);
  sleep(0.5);

  createProductDoubleFlow(sellerToken, storeId, testData);
  sleep(0.5);

  cleanupTestData(adminToken, testData);
}