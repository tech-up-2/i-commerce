import http from 'k6/http';
import {sendRequest} from "./send-request";
import {getAuthHeaders, getJsonHeaders} from "./hearders";

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

//로그인
export function login(email, password, options = {}) {
  const url = `${BASE_URL}/api/v1/auth/login`;

  const payload = JSON.stringify({
    email: email,
    password: password
  });

  const tagName = (options.tags && options.tags.name) || '로그인';
  const expectedStatuses = options.expectedStatuses || [200];

  return sendRequest('로그인', () => {
    const params = getJsonHeaders();
    params.tags = {name: tagName};

    return http.post(url, payload, params);
  }, expectedStatuses);
}

//토큰 재발급
export function tokenReissue(refreshToken, options = {}) {
  const url = `${BASE_URL}/api/v1/auth/reissue`;

  const payload = JSON.stringify({
    refreshToken: refreshToken
  });

  const tagName = (options.tags && options.tags.name) || '토큰재발급';

  return sendRequest('토큰 재발급', () => {
    const params = getJsonHeaders();
    params.tags = {name: tagName};

    return http.post(url, payload, params);
  }, [200]);
}

//토큰 테스트
export function tokenTest(accessToken, options = {}) {
  const url = `${BASE_URL}/api/v1/test/token`;

  const tagName = (options.tags && options.tags.name) || '토큰인증테스트';

  return sendRequest('토큰 인증 테스트', () => {
    const params = getAuthHeaders(accessToken);
    params.tags = {name: tagName};

    return http.get(url, params);
  }, [200]);
}

//비정상 로그인
export function failedLogin(email, wrongPassword, options = {}) {
  const url = `${BASE_URL}/api/v1/auth/login`;

  const payload = JSON.stringify({
    email: email,
    password: wrongPassword
  });

  const tagName = (options.tags && options.tags.name) || '비밀번호오류로그인';
  const expectedStatuses = options.expectedStatuses || [401];

  return sendRequest('비밀번호 오류 로그인', () => {
    const params = getJsonHeaders();
    params.tags = {name: tagName};

    return http.post(url, payload, params);
  }, expectedStatuses);
}