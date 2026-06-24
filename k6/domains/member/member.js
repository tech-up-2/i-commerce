import http from 'k6/http';
import {sendRequest} from "./send-request.js";
import {getAuthHeaders} from "./hearders.js";

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

// 회원 정보 조회
export function getMyInfo(accessToken, options = {}) {
  const url = `${BASE_URL}/api/v1/auth/users/me`;

  const tagName = options.tags?.name || '회원정보조회';

  return sendRequest('회원 정보 조회', () => {
    const params = getAuthHeaders(accessToken);
    params.tags = {name: tagName};

    return http.get(url, params);
  }, [200]);
}

// 회원 정보 수정
export function updateMyInfo(accessToken, updateRequest, options = {}) {
  const url = `${BASE_URL}/api/v1/auth/users/me`;

  const payload = JSON.stringify(updateRequest);

  const tagName = options.tags?.name || '회원정보조회';

  return sendRequest('회원 정보 조회', () => {
    const params = getAuthHeaders(accessToken);
    params.tags = {name: tagName};

    return http.patch(url, payload, params);
  }, [200]);
}

