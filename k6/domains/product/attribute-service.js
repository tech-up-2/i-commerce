import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ATTRIBUTE_BASE_PATH = `${BASE_URL}/api/v1/attributes`;

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
 * [POST] /api/v1/attributes
 * 속성 생성
 *
 * @param {string|null} token            - 인증 토큰 (canManageAttribute 권한 필요)
 * @param {Object}      payload          - CreateAttributeRequest
 * @param {string}      payload.key      - 속성 키 (필수, NotBlank)
 * @param {string[]}    payload.values   - 속성 값 목록 (필수, NotEmpty, 각 항목 NotBlank)
 * @returns {Response} k6 Response 객체
 */
export function createAttribute(token, payload) {
  return http.post(
      ATTRIBUTE_BASE_PATH,
      JSON.stringify(payload),
      buildParams(token, 'attribute_create'),
  );
}

/**
 * [GET] /api/v1/attributes
 * 전체 속성 조회 (키 기준 그룹핑)
 *
 * @param {string|null} token - 인증 토큰
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": [
 *     {
 *       "key": "소재",
 *       "values": [
 *         { "id": 1, "value": "면" },
 *         { "id": 2, "value": "폴리에스터" }
 *       ]
 *     }
 *   ]
 * }
 */
export function getAllAttributes(token) {
  return http.get(
      ATTRIBUTE_BASE_PATH,
      buildParams(token, 'attribute_get_all'),
  );
}