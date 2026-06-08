/**
 * Product Payload Builder
 * 상품 생성 요청 payload를 옵션 타입별로 생성
 */

/**
 * SKU 고유 prefix 생성
 */
function skuPrefix(type) {
  return `${type}-${__VU}-${Date.now()}`;
}

/**
 * NONE 타입 상품 생성 payload
 *
 * @param {number} storeId
 * @param {Object} testData - setupTestData() 반환값
 * @returns {Object} CreateProductRequest
 */
export function buildNoneProductPayload(storeId, testData) {
  return {
    storeId,
    name: `smoke_NONE_${__VU}_${Date.now()}`,
    description: 'NONE 타입 smoke test 상품',
    categoryId: testData.categoryId,
    productOptionType: 'NONE',
    mainImageUrl: 'https://example.com/main.jpg',
    imageUrls: [],
    options: [],
    items: [
      {
        optionValues: [],
        displayName: '기본',
        price: 10000,
        stock: 100,
        sku: skuPrefix('NONE'),
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
}

/**
 * SINGLE 타입 상품 생성 payload
 *
 * @param {number} storeId
 * @param {Object} testData
 * @returns {Object} CreateProductRequest
 */
export function buildSingleProductPayload(storeId, testData) {
  const prefix = skuPrefix('SINGLE');

  return {
    storeId,
    name: `smoke_SINGLE_${__VU}_${Date.now()}`,
    description: 'SINGLE 타입 smoke test 상품',
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
        sku: `${prefix}-RED`,
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
        sku: `${prefix}-BLUE`,
        attributes: null,
        isDefault: false,
      },
    ],
  };
}

/**
 * DOUBLE 타입 상품 생성 payload
 *
 * @param {number} storeId
 * @param {Object} testData
 * @returns {Object} CreateProductRequest
 */
export function buildDoubleProductPayload(storeId, testData) {
  const prefix = skuPrefix('DOUBLE');

  return {
    storeId,
    name: `smoke_DOUBLE_${__VU}_${Date.now()}`,
    description: 'DOUBLE 타입 smoke test 상품',
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
        optionValues: ['빨강', 'S'], displayName: '빨강/S',
        price: 10000, stock: 30, sku: `${prefix}-RED-S`,
        attributes: [{ attributeId: testData.attributeId, displayName: '소재', displayOrder: 1 }],
        isDefault: true,
      },
      {
        optionValues: ['빨강', 'M'], displayName: '빨강/M',
        price: 10000, stock: 30, sku: `${prefix}-RED-M`,
        attributes: null, isDefault: false,
      },
      {
        optionValues: ['파랑', 'S'], displayName: '파랑/S',
        price: 12000, stock: 30, sku: `${prefix}-BLUE-S`,
        attributes: null, isDefault: false,
      },
      {
        optionValues: ['파랑', 'M'], displayName: '파랑/M',
        price: 12000, stock: 30, sku: `${prefix}-BLUE-M`,
        attributes: null, isDefault: false,
      },
    ],
  };
}