

import http from 'k6/http';
import { buildParams } from '../../lib/http-helper.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * 카테고리-옵션 API base path를 반환
 *
 * @param {number} categoryId
 * @returns {string}
 */
function buildBasePath(categoryId) {
  return `${BASE_URL}/api/v1/categories/${categoryId}/options`;
}

/**
 * [GET] /api/v1/categories/{categoryId}/options
 * 특정 카테고리의 옵션 조회
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
 *     "options": [...]   ← CategoryOptionDto[]
 *   }
 * }
 */
export function getCategoryOptions(token, categoryId) {
  return http.get(
      buildBasePath(categoryId),
      buildParams(token, 'category_option_get'),
  );
}

/**
 * [POST] /api/v1/categories/{categoryId}/options
 * 카테고리에 옵션 추가
 *
 * @param {string}   token      - 인증 토큰 (canManageCategory 권한 필요)
 * @param {number}   categoryId - 옵션을 추가할 카테고리 ID
 * @param {Object}   payload    - AddCategoryOptionRequest
 * @param {number[]} payload.optionIds             - 추가할 옵션 ID 목록 (필수, NotEmpty, 중복 불가)
 * @param {boolean}  payload.propagateToChildren   - 하위 카테고리 전파 여부 (필수)
 * @param {boolean}  payload.required              - 필수 옵션 여부 (필수)
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": {
 *     "categoryId": 1,
 *     "skippedOptions": [        ← 이미 존재해서 건너뛴 옵션 목록
 *       { "categoryId": 1, "optionId": 2 }
 *     ]
 *   }
 * }
 */
export function addCategoryOption(token, categoryId, payload) {
  return http.post(
      buildBasePath(categoryId),
      JSON.stringify(payload),
      buildParams(token, 'category_option_add'),
  );
}

/**
 * [DELETE] /api/v1/categories/{categoryId}/options/{categoryOptionId}
 * 카테고리에서 옵션 제거
 *
 * @param {string} token            - 인증 토큰 (canManageCategory 권한 필요)
 * @param {number} categoryId       - 카테고리 ID
 * @param {number} categoryOptionId - 제거할 카테고리-옵션 연관 ID
 * @returns {Response} k6 Response 객체
 *
 */
export function deleteCategoryOption(token, categoryId, categoryOptionId) {
  return http.del(
      `${buildBasePath(categoryId)}/${categoryOptionId}`,
      null,
      buildParams(token, 'category_option_delete'),
  );
}