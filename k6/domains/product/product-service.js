import http from 'k6/http';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

// 도메인 함수: 태깅을 반드시 포함하고, 순수 HTTP 호출만 수행한다.
export function searchProducts(query, params = {}) {
  const url = `${BASE_URL}/api/products/search?q=${encodeURIComponent(query)}`;
  const requestParams = Object.assign({}, params, { tags: { name: 'product_search' } });
  return http.get(url, requestParams);
}

export function getProductDetails(productId, params = {}) {
  const url = `${BASE_URL}/api/products/${productId}`;
  const requestParams = Object.assign({}, params, { tags: { name: 'product_get' } });
  return http.get(url, requestParams);
}

export function createProduct(payload, params = {}) {
  const url = `${BASE_URL}/api/products`;
  const requestParams = Object.assign({}, params, {
	headers: Object.assign({ 'Content-Type': 'application/json' }, (params.headers || {})),
	tags: { name: 'product_create' }
  });
  return http.post(url, JSON.stringify(payload), requestParams);
}


