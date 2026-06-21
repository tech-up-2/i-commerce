import http from 'k6/http';
import {sendRequest} from './send-request.js';
import {getAuthHeaders} from "./hearders";

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

//배송지 목록 조회
export function getMyAddresses(accessToken, options = {}) {
  const url = `${BASE_URL}/api/v1/members/delivery-addresses`;

  const tagName = options.tags?.name || '배송지목록조회';

  return sendRequest('배송지 목록 조회', () => {
    const params = getAuthHeaders(accessToken);
    params.tags = {name: tagName};

    return http.get(url, params);
  }, [200]);
}