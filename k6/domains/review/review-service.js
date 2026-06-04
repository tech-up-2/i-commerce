import http from 'k6/http';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

export function createReview(orderProductId, token, payload, params = {}){
  const url = `${BASE_URL}/api/v1/order-products/${orderProductId}/reviews`;
  const requestBody = {
    review: http.file(JSON.stringify(payload), 'review.json', 'application/json'),
  }
  const requestParams = Object.assign({}, params, {
    headers : Object.assign({
      'Authorization': `Bearer ${token}`,
    }, (params.headers || {})),
    tags: {name: 'review_create_post'},
  });
  return http.post(url, requestBody, requestParams);
}

export function viewList(productId, params = {}){
  const url = `${BASE_URL}/api/v1/reviews?productId=${productId}`;
  const requestParams = Object.assign({}, params, {tags: {name: 'review_list_get'}});
  return http.get(url, requestParams);
}

export function viewDetailReview(reviewId, params = {}){
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}`;
  const requestParams = Object.assign({}, params, {tags: {name: 'review_detail_get'}});
  return http.get(url, requestParams);
}

export function searchReviews(queryParams, params = {}){
  const queryString = Object.keys(queryParams)
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&');
  const url = `${BASE_URL}/api/v1/reviews/search?${queryString}`;
  const requestParams = Object.assign({}, params, {tags: {name: 'review_search_get'}})
  return http.get(url, requestParams);
}

export function getReviewStats(productId, params = {}){
  const url = `${BASE_URL}/api/v1/reviews/products/${productId}/stats`;
  const requestParams = Object.assign({}, params, {tags: {name: 'review_stats_get'}})
  return http.get(url, requestParams);
}

export function toggleLike(reviewId, token, params = {}){
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}/likes`;
  const requestParams = Object.assign({}, params, {
    headers : Object.assign({
      'Authorization': `Bearer ${token}`,
    }, (params.headers || {})),
    tags: {name: 'review_likes_post'}
  });
  return http.post(url, null, requestParams);
}

export function createReport(reviewId, token, payload, params = {}){
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}/reports`;
  const requestParams = Object.assign({}, params, {
  headers : Object.assign({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
    }, (params.headers || {})),
    tags: {name: 'review_reports_post'}
  });
  const requestBody = JSON.stringify(payload);
  return http.post(url, requestBody, requestParams);
}

export function approveReport(reportId, token, params = {}){
  const url = `${BASE_URL}/api/v1/admin/reviews/reports/${reportId}/approve`;
  const requestParams = Object.assign({}, params, {
    headers : Object.assign({
      'Authorization': `Bearer ${token}`
      }, (params.headers || {})),
      tags: {name: 'report_approve_patch'}
  });
  return http.patch(url, null, requestParams);
}

export function rejectReport(reportId, token, params = {}){
  const url = `${BASE_URL}/api/v1/admin/reviews/reports/${reportId}/reject`;
  const requestParams = Object.assign({}, params, {
    headers : Object.assign({
      'Authorization': `Bearer ${token}`
      }, (params.headers || {})),
      tags: {name: 'report_reject_patch'}
  });
  return http.patch(url, null, requestParams);
}

