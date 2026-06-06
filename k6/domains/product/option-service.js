import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const OPTION_BASE_PATH = `${BASE_URL}/api/v1/options`;

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
 * [POST] /api/v1/options
 * 옵션 생성
 *
 * @param {string} token             - 인증 토큰 (canManageOption 권한 필요)
 * @param {Object} payload           - CreateOptionRequest
 * @param {string} payload.name      - 옵션명 (필수, NotBlank)
 * @param {string} payload.inputType - 입력 방식 (SELECT | RADIO)
 * @returns {Response} k6 Response 객체
 */
export function createOption(token, payload) {
  return http.post(
      OPTION_BASE_PATH,
      JSON.stringify(payload),
      buildParams(token, 'option_create'),
  );
}

/**
 * [GET] /api/v1/options
 * 전체 옵션 조회
 *
 * @param {string} token - 인증 토큰 (canManageOption 권한 필요)
 * @returns {Response} k6 Response 객체
 */
export function getAllOptions(token) {
  return http.get(
      OPTION_BASE_PATH,
      buildParams(token, 'option_get_all'),
  );
}

/**
 * [DELETE] /api/v1/options/{optionId}
 * 옵션 삭제
 *
 * @param {string} token    - 인증 토큰 (canManageOption 권한 필요)
 * @param {number} optionId - 삭제할 옵션 ID
 * @returns {Response} k6 Response 객체
 */
export function deleteOption(token, optionId) {
  return http.del(
      `${OPTION_BASE_PATH}/${optionId}`,
      null,
      buildParams(token, 'option_delete'),
  );
}