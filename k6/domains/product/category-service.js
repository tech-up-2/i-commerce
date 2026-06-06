import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const CATEGORY_BASE_PATH = `${BASE_URL}/api/v1/categories`;

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
 * [POST] /api/v1/categories
 * 카테고리 생성
 *
 * @param {string}      token              - 인증 토큰 (canManageCategory 권한 필요)
 * @param {Object}      payload            - CreateCategoryRequest
 * @param {number|null} payload.parentId   - 부모 카테고리 ID (null이면 최상위 카테고리)
 * @param {string}      payload.name       - 카테고리 이름 (필수, NotBlank)
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": { "id": 1 }   ← 생성된 카테고리 ID
 * }
 */
export function createCategory(token, payload) {
  return http.post(
      CATEGORY_BASE_PATH,
      JSON.stringify(payload),
      buildParams(token, 'category_create'),
  );
}

/**
 * [GET] /api/v1/categories
 * 전체 카테고리 조회
 *
 * @param {string|null} token    - 인증 토큰 (인가 불필요)
 * @param {number|null} maxDepth - 조회할 최대 깊이 (null이면 전체 조회)
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조: (계층형 트리)
 * {
 *   "code": "SUCCESS",
 *   "data": [
 *     {
 *       "id": 1,
 *       "parentId": null,
 *       "name": "의류",
 *       "depth": 0,
 *       "children": [
 *         { "id": 2, "parentId": 1, "name": "상의", "depth": 1, "children": [] }
 *       ]
 *     }
 *   ]
 * }
 */
export function getAllCategories(token, maxDepth = null) {
  const url = maxDepth !== null
      ? `${CATEGORY_BASE_PATH}?maxDepth=${maxDepth}`
      : CATEGORY_BASE_PATH;

  return http.get(url, buildParams(token, 'category_get_all'));
}

/**
 * [GET] /api/v1/categories/{categoryId}
 * 특정 카테고리 조회
 *
 * @param {string|null} token      - 인증 토큰 (인가 불필요)
 * @param {number}      categoryId - 조회할 카테고리 ID
 * @returns {Response} k6 Response 객체
 */
export function getCategory(token, categoryId) {
  return http.get(
      `${CATEGORY_BASE_PATH}/${categoryId}`,
      buildParams(token, 'category_get'),
  );
}

/**
 * [DELETE] /api/v1/categories/{categoryId}
 * 특정 카테고리 삭제
 *
 * @param {string} token      - 인증 토큰 (canManageCategory 권한 필요)
 * @param {number} categoryId - 삭제할 카테고리 ID
 * @returns {Response} k6 Response 객체
 */
export function deleteCategory(token, categoryId) {
  return http.del(
      `${CATEGORY_BASE_PATH}/${categoryId}`,
      null,
      buildParams(token, 'category_delete'),
  );
}