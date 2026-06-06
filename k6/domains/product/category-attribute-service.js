import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 *
 * @param {number} categoryId
 * @returns {string}
 */
function buildBasePath(categoryId) {
  return `${BASE_URL}/api/v1/categories/${categoryId}/attributes`;
}

/**
 * 공통 파라미터 생성
 *
 * @param {string|null} token   - Bearer 토큰
 * @param {string}      tagName - 메트릭 태그명
 * @returns {Object} k6 params 객체
 */
function buildParams(token, tagName) {
  const headers = { 'Content-Type': 'application/json' };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return {
    headers,
    tags: { name: tagName },
  };
}

/**
 * [GET] /api/v1/categories/{categoryId}/attributes
 * 특정 카테고리의 속성 조회
 *
 * @param {string|null} token      - 인증 토큰 (인가 불필요)
 * @param {number}      categoryId - 조회할 카테고리 ID
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": {
 *     "categoryId": 1,
 *     "attributes": [...]
 *   }
 * }
 */
export function getCategoryAttributes(token, categoryId) {
  return http.get(
      buildBasePath(categoryId),
      buildParams(token, 'category_attribute_get'),
  );
}

/**
 * [POST] /api/v1/categories/{categoryId}/attributes
 * 카테고리에 속성 추가
 *
 * @param {string}   token      - 인증 토큰 (canManageCategory 권한 필요)
 * @param {number}   categoryId - 속성을 추가할 카테고리 ID
 * @param {Object}   payload    - AddCategoryAttributeRequest
 * @param {number[]} payload.attributeIds        - 추가할 속성 ID 목록 (필수, NotEmpty, 중복 불가)
 * @param {boolean}  payload.propagateToChildren - 하위 카테고리 전파 여부 (필수)
 * @param {boolean}  payload.required            - 필수 속성 여부 (필수)
 * @returns {Response} k6 Response 객체
 *
 */
export function addCategoryAttribute(token, categoryId, payload) {
  return http.post(
      buildBasePath(categoryId),
      JSON.stringify(payload),
      buildParams(token, 'category_attribute_add'),
  );
}

/**
 * [DELETE] /api/v1/categories/{categoryId}/attributes/{categoryAttributeId}
 * 카테고리에서 속성 제거
 *
 * @param {string} token               - 인증 토큰 (canManageCategory 권한 필요)
 * @param {number} categoryId          - 카테고리 ID
 * @param {number} categoryAttributeId - 제거할 카테고리-속성 연관 ID
 * @returns {Response} k6 Response 객체
 *
 */
export function deleteCategoryAttribute(token, categoryId, categoryAttributeId) {
  return http.del(
      `${buildBasePath(categoryId)}/${categoryAttributeId}`,
      null,
      buildParams(token, 'category_attribute_delete'),
  );
}