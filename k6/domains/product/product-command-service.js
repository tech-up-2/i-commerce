import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUCT_BASE_PATH = `${BASE_URL}/api/v1/products`;

/**
 * 공통 파라미터 생성
 *
 * @param {string} token   - Bearer 토큰
 * @param {string} tagName - 메트릭 태그명
 * @returns {Object} k6 params 객체
 */
function buildParams(token, tagName) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    tags: { name: tagName },
  };
}

/**
 * [POST] /api/v1/products
 * 상품 생성
 *
 * @param {string} token   - 인증 토큰 (storeManager 권한 필요)
 * @param {Object} payload - CreateProductRequest
 *
 * payload 구조 (ProductOptionType별):
 *
 * [NONE] 옵션 없음
 * {
 *   storeId: 1,
 *   name: "상품명",
 *   description: "설명",
 *   categoryId: 1,
 *   productOptionType: "NONE",
 *   mainImageUrl: "https://...",
 *   imageUrls: [],
 *   options: [],
 *   items: [
 *     {
 *       optionValues: [],
 *       displayName: "기본",
 *       price: 10000,
 *       stock: 100,
 *       sku: "SKU-001",
 *       attributes: [{ attributeId: 1, displayName: "소재", displayOrder: 1 }],
 *       isDefault: true
 *     }
 *   ]
 * }
 *
 * [SINGLE] 옵션 1개
 * {
 *   ...
 *   productOptionType: "SINGLE",
 *   options: [
 *     {
 *       optionOrder: 1,
 *       optionId: 1,
 *       name: "색상",
 *       values: [
 *         { value: "빨강", displayOrder: 0 },
 *         { value: "파랑", displayOrder: 1 }
 *       ]
 *     }
 *   ],
 *   items: [
 *     { optionValues: ["빨강"], price: 10000, stock: 50, sku: "SKU-RED", isDefault: true },
 *     { optionValues: ["파랑"], price: 10000, stock: 50, sku: "SKU-BLUE", isDefault: false }
 *   ]
 * }
 *
 * [DOUBLE] 옵션 2개
 * {
 *   ...
 *   productOptionType: "DOUBLE",
 *   options: [
 *     { optionOrder: 1, optionId: 1, name: "색상", values: [...] },
 *     { optionOrder: 2, optionId: 2, name: "사이즈", values: [...] }
 *   ],
 *   items: [
 *     { optionValues: ["빨강", "S"], price: 10000, stock: 30, sku: "SKU-RED-S", isDefault: true },
 *     { optionValues: ["빨강", "M"], price: 10000, stock: 30, sku: "SKU-RED-M", isDefault: false },
 *     ...
 *   ]
 * }
 *
 * @returns {Response} k6 Response 객체
 */
export function createProduct(token, payload) {
  return http.post(
      PRODUCT_BASE_PATH,
      JSON.stringify(payload),
      buildParams(token, 'product_create'),
  );
}

/**
 * [PATCH] /api/v1/products/{productId}
 * 상품 기본 정보 수정 (name, description)
 *
 * @param {string} token     - 셀러 토큰 (storeManager 권한 필요)
 * @param {number} productId - 수정할 상품 ID
 * @param {Object} payload   - UpdateProductRequest
 * @param {string} payload.name        - 상품명 (필수, NotBlank, max 100)
 * @param {string} payload.description - 상품 설명 (선택, max 1000)
 * @returns {Response} k6 Response 객체
 */
export function updateProduct(token, productId, payload) {
  return http.patch(
      `${PRODUCT_BASE_PATH}/${productId}`,
      JSON.stringify(payload),
      buildParams(token, 'product_update'),
  );
}

/**
 * [PATCH] /api/v1/products/{productId}/status
 * 상품 상태 변경
 *
 * 상태 전이 규칙:
 *   PENDING      → ON_SALE, DISCONTINUED
 *   ON_SALE      → PENDING, DISCONTINUED
 *   DISCONTINUED → PENDING
 *
 * @param {string} token     - 셀러 토큰 (storeManager 권한 필요)
 * @param {number} productId - 상태를 변경할 상품 ID
 * @param {Object} payload   - UpdateProductStatusRequest
 * @param {string} payload.status - 변경할 상태 (ProductStatus Enum)
 * @returns {Response} k6 Response 객체
 */
export function changeProductStatus(token, productId, payload) {
  return http.patch(
      `${PRODUCT_BASE_PATH}/${productId}/status`,
      JSON.stringify(payload),
      buildParams(token, 'product_change_status'),
  );
}