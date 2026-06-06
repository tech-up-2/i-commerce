/**
 * Product Helper - productId 획득 통합 인터페이스
 *
 * 타 도메인에서 productId가 필요할 때 이 파일만 import
 *
 * 사용 예시:
 *   const result = createTestProduct(adminToken, sellerToken, storeId, 'DOUBLE');
 *   if (!result) return;
 *   const { productId, testData } = result;
 *   // productId로 본인 시나리오 실행
 *   cleanupTestProduct(adminToken, testData);
 */
import { sleep } from 'k6';
import { createCategory, deleteCategory } from '../domains/product/category-service.js';
import { createOption, getAllOptions, deleteOption } from '../domains/product/option-service.js';
import { createAttribute, getAllAttributes } from '../domains/product/attribute-service.js';
import { addCategoryOption } from '../domains/product/category-option-service.js';
import { addCategoryAttribute } from '../domains/product/category-attribute-service.js';
import { createProduct } from '../domains/product/product-command-service.js';
import {
  buildNoneProductPayload,
  buildSingleProductPayload,
  buildDoubleProductPayload,
} from './product-payload-builder.js';

/**
 * 테스트용 사전 데이터 세팅
 * 카테고리, 옵션(2개), 속성을 생성하고 카테고리에 연결합니다.
 *
 * @param {string} adminToken
 * @returns {Object|null}
 */
export function setupTestData(adminToken) {
  const categoryRes = createCategory(adminToken, {
    parentId: null,
    name: `smoke_category_${__VU}_${Date.now()}`,
  });

  if (categoryRes.status !== 200) {
    console.error(`[Setup] 카테고리 생성 실패 | status: ${categoryRes.status}`);
    return null;
  }

  const categoryId = JSON.parse(categoryRes.body).data.id;
  sleep(0.3);

  createOption(adminToken, { name: `smoke_option1_${__VU}_${Date.now()}`, inputType: 'SELECT' });
  sleep(0.3);
  createOption(adminToken, { name: `smoke_option2_${__VU}_${Date.now()}`, inputType: 'RADIO' });
  sleep(0.3);

  const allOptions = JSON.parse(getAllOptions(adminToken).body).data;

  const option1 = allOptions
  .filter((o) => o.name.startsWith('smoke_option1') && o.inputType === 'SELECT')
  .sort((a, b) => b.id - a.id)[0];

  const option2 = allOptions
  .filter((o) => o.name.startsWith('smoke_option2') && o.inputType === 'RADIO')
  .sort((a, b) => b.id - a.id)[0];

  if (!option1 || !option2) {
    console.error('[Setup] 옵션 id 추출 실패');
    deleteCategory(adminToken, categoryId);
    return null;
  }

  sleep(0.3);

  const attrKey = `smoke_attr_${__VU}_${Date.now()}`;
  createAttribute(adminToken, { key: attrKey, values: ['면', '폴리에스터'] });
  sleep(0.3);

  const allAttr = JSON.parse(getAllAttributes(adminToken).body).data;
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
  sleep(0.3);

  const addOptRes = addCategoryOption(adminToken, categoryId, {
    optionIds: [option1.id, option2.id],
    propagateToChildren: false,
    required: false,
  });

  if (addOptRes.status !== 200) {
    console.error('[Setup] 카테고리-옵션 추가 실패');
    deleteOption(adminToken, option1.id);
    deleteOption(adminToken, option2.id);
    deleteCategory(adminToken, categoryId);
    return null;
  }

  sleep(0.3);

  const addAttrRes = addCategoryAttribute(adminToken, categoryId, {
    attributeIds: [attributeId],
    propagateToChildren: false,
    required: false,
  });

  if (addAttrRes.status !== 200) {
    console.error('[Setup] 카테고리-속성 추가 실패');
    deleteOption(adminToken, option1.id);
    deleteOption(adminToken, option2.id);
    deleteCategory(adminToken, categoryId);
    return null;
  }

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
 * 사후 정리
 *
 * @param {string} adminToken
 * @param {Object} testData
 */
export function cleanupTestData(adminToken, testData) {
  if (!testData) return;
  deleteOption(adminToken, testData.option1Id);
  sleep(0.2);
  deleteOption(adminToken, testData.option2Id);
  sleep(0.2);
  deleteCategory(adminToken, testData.categoryId);
}

/**
 * 테스트용 상품 생성 및 productId 반환
 *
 * @param {string} adminToken
 * @param {string} sellerToken
 * @param {number} storeId
 * @param {'NONE'|'SINGLE'|'DOUBLE'} optionType
 * @returns {{ productId: number, testData: Object } | null}
 */
export function createTestProduct(adminToken, sellerToken, storeId, optionType = 'NONE') {
  const testData = setupTestData(adminToken);

  if (!testData) return null;

  const payloadBuilders = {
    NONE:   () => buildNoneProductPayload(storeId, testData),
    SINGLE: () => buildSingleProductPayload(storeId, testData),
    DOUBLE: () => buildDoubleProductPayload(storeId, testData),
  };

  const buildPayload = payloadBuilders[optionType];

  if (!buildPayload) {
    console.error(`[createTestProduct] 알 수 없는 optionType: ${optionType}`);
    cleanupTestData(adminToken, testData);
    return null;
  }

  const res = createProduct(sellerToken, buildPayload());

  if (res.status !== 200) {
    console.error(`[createTestProduct] 상품 생성 실패 | status: ${res.status}`);
    cleanupTestData(adminToken, testData);
    return null;
  }

  const productId = JSON.parse(res.body).data.productId;
  return { productId, testData };
}

/**
 * 사후 정리 (createTestProduct와 쌍으로 사용)
 *
 * @param {string} adminToken
 * @param {Object} testData
 */
export function cleanupTestProduct(adminToken, testData) {
  cleanupTestData(adminToken, testData);
}