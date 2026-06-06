/**
 * HTTP 공통 파라미터 생성 헬퍼
 *
 * @param {string|null} token   - Bearer 토큰 (없으면 Authorization 헤더 생략)
 * @param {string}      tagName - 메트릭 태그명
 * @returns {Object} k6 params 객체
 */
export function buildParams(token, tagName) {
  const headers = { 'Content-Type': 'application/json' };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return {
    headers,
    tags: { name: tagName },
  };
}