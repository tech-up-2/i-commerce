/**
 * Product Create Scenarios
 *
 * 토큰 역할:
 *   ADMIN_TOKEN  → 사전 준비 (카테고리/옵션/속성 생성 및 연결)
 *   TEST_TOKEN   → 상품 생성
 *
 * 사전 조건:
 *   - ADMIN_TOKEN:    관리자 권한 토큰
 *   - TEST_TOKEN:     storeManager 권한을 가진 셀러 토큰
 *   - TEST_STORE_ID:  해당 셀러 토큰의 userId가 manager로 등록된 스토어 ID
 */
import { check, sleep } from 'k6';
import { createCategory, deleteCategory } from '../domains/product/category-service.js';
import { createOption, getAllOptions, deleteOption } from '../domains/product/option-service.js';
import { createAttribute, getAllAttributes } from '../domains/product/attribute-service.js';
import { addCategoryOption } from '../domains/product/category-option-service.js';
import { addCategoryAttribute } from '../domains/product/category-attribute-service.js';
import { createProduct } from '../domains/product/product-command-service.js';

/**
 * 고유 SKU 생성 헬퍼
 *
 * @param {string} prefix
 * @returns {string}
 */
function generateSku(prefix) {
  return `${prefix}-${__VU}-${Date.now()}`;
}

/**
 * 상품 생성 사전 준비
 *
 * @param {string} adminToken - 관리자 토큰
 * @returns {Object|null} { categoryId, option1Id, option1Name, option2Id, option2Name, attributeId }
 */
export function setupProductTestData(adminToken) {

  // 카테고리 생성

  const categoryRes = createCategory(adminToken, {
    parentId: null,
    name: `스모크테스트_상품용_카테고리_${__VU}_${Date.now()}`,
  });

  if (categoryRes.status !== 200) {
    console.error(`[Setup] 카테고리 생성 실패 | status: ${categoryRes.status} | body: ${categoryRes.body}`);
    return null;
  }

  const categoryId = JSON.parse(categoryRes.body).data.id;
  console.log(`[Setup] 카테고리 생성 완료 | categoryId: ${categoryId}`);

  sleep(0.3);

  // 옵션 1 생성 (색상, SELECT)

  createOption(adminToken, {
    name: `스모크테스트_색상_${__VU}_${Date.now()}`,
    inputType: 'SELECT',
  });

  sleep(0.3);

  // 옵션 2 생성 (사이즈, RADIO)

  createOption(adminToken, {
    name: `스모크테스트_사이즈_${__VU}_${Date.now()}`,
    inputType: 'RADIO',
  });

  sleep(0.3);

  // 생성한 옵션 id 조회

  const allOptionsRes = getAllOptions(adminToken);
  const allOptions = JSON.parse(allOptionsRes.body).data;

  const option1 = allOptions
  .filter((o) => o.name.startsWith('스모크테스트_색상') && o.inputType === 'SELECT')
  .sort((a, b) => b.id - a.id)[0];

  const option2 = allOptions
  .filter((o) => o.name.startsWith('스모크테스트_사이즈') && o.inputType === 'RADIO')
  .sort((a, b) => b.id - a.id)[0];

  if (!option1 || !option2) {
    console.error('[Setup] 옵션 id 추출 실패');
    deleteCategory(adminToken, categoryId);
    return null;
  }

  console.log(`[Setup] 옵션 id 추출 완료 | option1Id: ${option1.id}, option2Id: ${option2.id}`);

  sleep(0.3);

  // 속성 생성

  const attrKey = `스모크테스트_소재_${__VU}_${Date.now()}`;
  createAttribute(adminToken, { key: attrKey, values: ['면', '폴리에스터'] });

  sleep(0.3);

  // 생성한 속성 id 조회

  const allAttrRes = getAllAttributes(adminToken);
  const allAttr = JSON.parse(allAttrRes.body).data;

  const attrGroup = allAttr
  .filter((g) => g.key === attrKey)
  .sort((a, b) => b.values[0].id - a.values[0].id)[0];

  if (!attrGroup || attrGroup.values.length === 0) {
    console.error('[Setup] 속성 id 추출 실패');
    deleteOption(adminToken, option1.id);
    deleteOption(adminToken, option2.id);
    deleteCategory(adminToken, categoryId);
    return null;
  }

  const attributeId = attrGroup.values[0].id;
  console.log(`[Setup] 속성 id 추출 완료 | attributeId: ${attributeId}`);

  sleep(0.3);

  // 카테고리에 옵션 추가

  const addOptRes = addCategoryOption(adminToken, categoryId, {
    optionIds: [option1.id, option2.id],
    propagateToChildren: false,
    required: false,
  });

  if (addOptRes.status !== 200) {
    console.error(`[Setup] 카테고리-옵션 추가 실패 | status: ${addOptRes.status} | body: ${addOptRes.body}`);
    deleteOption(adminToken, option1.id);
    deleteOption(adminToken, option2.id);
    deleteCategory(adminToken, categoryId);
    return null;
  }

  console.log('[Setup] 카테고리-옵션 추가 완료');

  sleep(0.3);

  // 카테고리에 속성 추가

  const addAttrRes = addCategoryAttribute(adminToken, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  if (addAttrRes.status !== 200) {
    console.error(`[Setup] 카테고리-속성 추가 실패 | status: ${addAttrRes.status} | body: ${addAttrRes.body}`);
    deleteOption(adminToken, option1.id);
    deleteOption(adminToken, option2.id);
    deleteCategory(adminToken, categoryId);
    return null;
  }

  console.log('[Setup] 카테고리-속성 추가 완료');

  return {
    categoryId,
    option1Id: option1.id,
    option1Name: option1.name,
    option2Id: option2.id,
    option2Name: option2.name,
    attributeId,
  };
}

/**
 * 사후 정리 헬퍼
 *
 * @param {string} adminToken
 * @param {Object} testData
 */
export function cleanupProductTestData(adminToken, testData) {
  if (!testData) return;

  console.log('[Cleanup] 사후 정리 시작');

  deleteOption(adminToken, testData.option1Id);
  sleep(0.2);

  deleteOption(adminToken, testData.option2Id);
  sleep(0.2);

  deleteCategory(adminToken, testData.categoryId);
  console.log('[Cleanup] 사후 정리 완료');
}

/**
 * [케이스 1] NONE 타입 상품 생성 플로우
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductNoneFlow(sellerToken, storeId, testData) {
  console.log('[Flow] NONE 타입 상품 생성 요청');

  const payload = {
    storeId,
    name: `스모크테스트_NONE상품_${__VU}_${Date.now()}`,
    description: 'NONE 타입 smoke test 상품입니다.',
    categoryId: testData.categoryId,
    productOptionType: 'NONE',
    mainImageUrl: 'https://example.com/main.jpg',
    imageUrls: ['https://example.com/1.jpg'],
    options: [],
    items: [
      {
        optionValues: [],
        displayName: '기본',
        price: 10000,
        stock: 100,
        sku: generateSku('NONE'),
        attributes: [
          {
            attributeId: testData.attributeId,
            displayName: '소재',
            displayOrder: 1,
          },
        ],
        isDefault: true,
      },
    ],
  };

  const res = createProduct(sellerToken, payload);

  console.log(`[Flow] createProduct (NONE) | status: ${res.status} | body: ${res.body}`);

  const isSuccess = check(res, {
    '[createProduct/NONE] status is 200': (r) => r.status === 200,
    '[createProduct/NONE] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[createProduct/NONE] 응답에 productId 존재': (r) => {
      try {
        return typeof JSON.parse(r.body).data.productId === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isSuccess) return null;

  const productId = JSON.parse(res.body).data.productId;
  console.log(`[Flow] NONE 타입 상품 생성 완료 | productId: ${productId}`);
  return productId;
}

/**
 * [케이스 2] SINGLE 타입 상품 생성 플로우
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductSingleFlow(sellerToken, storeId, testData) {
  console.log('[Flow] SINGLE 타입 상품 생성 요청');

  const skuPrefix = `SINGLE-${__VU}-${Date.now()}`;

  const payload = {
    storeId,
    name: `스모크테스트_SINGLE상품_${__VU}_${Date.now()}`,
    description: 'SINGLE 타입 smoke test 상품입니다.',
    categoryId: testData.categoryId,
    productOptionType: 'SINGLE',
    mainImageUrl: 'https://example.com/main.jpg',
    imageUrls: [],
    options: [
      {
        optionOrder: 1,
        optionId: testData.option1Id,
        name: testData.option1Name,
        values: [
          { value: '빨강', displayOrder: 0 },
          { value: '파랑', displayOrder: 1 },
        ],
      },
    ],
    items: [
      {
        optionValues: ['빨강'],
        displayName: '빨강',
        price: 10000,
        stock: 50,
        sku: `${skuPrefix}-RED`,
        attributes: [
          {
            attributeId: testData.attributeId,
            displayName: '소재',
            displayOrder: 1,
          },
        ],
        isDefault: true,
      },
      {
        optionValues: ['파랑'],
        displayName: '파랑',
        price: 10000,
        stock: 50,
        sku: `${skuPrefix}-BLUE`,
        attributes: null,
        isDefault: false,
      },
    ],
  };

  const res = createProduct(sellerToken, payload);

  console.log(`[Flow] createProduct (SINGLE) | status: ${res.status} | body: ${res.body}`);

  const isSuccess = check(res, {
    '[createProduct/SINGLE] status is 200': (r) => r.status === 200,
    '[createProduct/SINGLE] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[createProduct/SINGLE] 응답에 productId 존재': (r) => {
      try {
        return typeof JSON.parse(r.body).data.productId === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isSuccess) return null;

  const productId = JSON.parse(res.body).data.productId;
  console.log(`[Flow] SINGLE 타입 상품 생성 완료 | productId: ${productId}`);
  return productId;
}

/**
 * [케이스 3] DOUBLE 타입 상품 생성 플로우
 *
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {Object} testData
 * @returns {number|null} productId
 */
export function createProductDoubleFlow(sellerToken, storeId, testData) {
  console.log('[Flow] DOUBLE 타입 상품 생성 요청');

  const skuPrefix = `DOUBLE-${__VU}-${Date.now()}`;

  const payload = {
    storeId,
    name: `스모크테스트_DOUBLE상품_${__VU}_${Date.now()}`,
    description: 'DOUBLE 타입 smoke test 상품입니다.',
    categoryId: testData.categoryId,
    productOptionType: 'DOUBLE',
    mainImageUrl: 'https://example.com/main.jpg',
    imageUrls: [],
    options: [
      {
        optionOrder: 1,
        optionId: testData.option1Id,
        name: testData.option1Name,
        values: [
          { value: '빨강', displayOrder: 0 },
          { value: '파랑', displayOrder: 1 },
        ],
      },
      {
        optionOrder: 2,
        optionId: testData.option2Id,
        name: testData.option2Name,
        values: [
          { value: 'S', displayOrder: 0 },
          { value: 'M', displayOrder: 1 },
        ],
      },
    ],
    items: [
      {
        optionValues: ['빨강', 'S'],
        displayName: '빨강/S',
        price: 10000,
        stock: 30,
        sku: `${skuPrefix}-RED-S`,
        attributes: [
          {
            attributeId: testData.attributeId,
            displayName: '소재',
            displayOrder: 1,
          },
        ],
        isDefault: true,
      },
      {
        optionValues: ['빨강', 'M'],
        displayName: '빨강/M',
        price: 10000,
        stock: 30,
        sku: `${skuPrefix}-RED-M`,
        attributes: null,
        isDefault: false,
      },
      {
        optionValues: ['파랑', 'S'],
        displayName: '파랑/S',
        price: 12000,
        stock: 30,
        sku: `${skuPrefix}-BLUE-S`,
        attributes: null,
        isDefault: false,
      },
      {
        optionValues: ['파랑', 'M'],
        displayName: '파랑/M',
        price: 12000,
        stock: 30,
        sku: `${skuPrefix}-BLUE-M`,
        attributes: null,
        isDefault: false,
      },
    ],
  };

  const res = createProduct(sellerToken, payload);

  console.log(`[Flow] createProduct (DOUBLE) | status: ${res.status} | body: ${res.body}`);

  const isSuccess = check(res, {
    '[createProduct/DOUBLE] status is 200': (r) => r.status === 200,
    '[createProduct/DOUBLE] code is SUCCESS': (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    '[createProduct/DOUBLE] 응답에 productId 존재': (r) => {
      try {
        return typeof JSON.parse(r.body).data.productId === 'number';
      } catch {
        return false;
      }
    },
  });

  if (!isSuccess) return null;

  const productId = JSON.parse(res.body).data.productId;
  console.log(`[Flow] DOUBLE 타입 상품 생성 완료 | productId: ${productId}`);
  return productId;
}

/**
 * 상품 생성 헬퍼
 *
 * 사용 방법:
 *   1. createTestProduct()로 상품 생성 및 productId 획득
 *   2. 획득한 productId로 원하는 시나리오 실행
 *   3. 시나리오 종료 후 cleanupProductTestData()로 정리
 *
 * 예시:
 *   const { productId, testData } = createTestProduct(adminToken, sellerToken, storeId, 'DOUBLE');
 *   cleanupProductTestData(adminToken, testData);
 *
 * @param {string} adminToken
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {'NONE'|'SINGLE'|'DOUBLE'} optionType - 생성할 상품의 옵션 타입
 * @returns {{ productId: number, testData: Object } | null}
 */
export function createTestProduct(adminToken, sellerToken, storeId, optionType = 'NONE') {

  const testData = setupProductTestData(adminToken);

  if (!testData) {
    console.error('[createTestProduct] 사전 준비 실패.');
    return null;
  }

  const flowMap = {
    NONE:   () => createProductNoneFlow(sellerToken, storeId, testData),
    SINGLE: () => createProductSingleFlow(sellerToken, storeId, testData),
    DOUBLE: () => createProductDoubleFlow(sellerToken, storeId, testData),
  };

  const createFlow = flowMap[optionType];

  if (!createFlow) {
    console.error(`[createTestProduct] 알 수 없는 optionType: ${optionType}`);
    cleanupProductTestData(adminToken, testData);
    return null;
  }

  const productId = createFlow();

  if (!productId) {
    console.error(`[createTestProduct] ${optionType} 타입 상품 생성 실패.`);
    cleanupProductTestData(adminToken, testData);
    return null;
  }

  console.log(`[createTestProduct] 완료 | optionType: ${optionType} | productId: ${productId}`);

  return { productId, testData };
}