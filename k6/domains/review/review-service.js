import http from 'k6/http';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';
const imagePath = __ENV.TEST_IMAGE_PATH || './data/test.png';
const binImage = open('../../data/test.png', 'b');

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

/* =========================================================================
 * 리뷰 기본 CRUD, 조회 API
 * ========================================================================= */

// 리뷰 생성 (Multipart/form-data)
export function createReview(orderProductId, token, payload) {
  const url = `${BASE_URL}/api/v1/order-products/${orderProductId}/reviews`;

  const requestBody = {
    review: http.file(JSON.stringify(payload), 'review.json', 'application/json'),
    imageFiles: http.file(binImage, 'test.png', 'image/png'),
  };

  const requestParams = buildParams(token, 'review_create_post');

  if (requestParams.headers) {
    delete requestParams.headers['Content-Type'];
  }

  return http.post(url, requestBody, requestParams);
}

// 리뷰 목록 조회
export function viewList(productId) {
  const url = `${BASE_URL}/api/v1/reviews?productId=${productId}`;
  return http.get(url, buildParams(null, 'review_list_get'));
}

// 리뷰 상세 조회
export function viewDetailReview(reviewId) {
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}`;
  return http.get(url, buildParams(null, 'review_detail_get'));
}

// 리뷰 검색
export function searchReviews(queryParams) {
  const queryString = Object.keys(queryParams)
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&');
  const url = `${BASE_URL}/api/v1/reviews/search?${queryString}`;
  return http.get(url, buildParams(null, 'review_search_get'));
}

// 리뷰 통계 조회
export function getReviewStats(productId) {
  const url = `${BASE_URL}/api/v1/reviews/products/${productId}/stats`;
  return http.get(url, buildParams(null, 'review_stats_get'));
}

// 리뷰 답글 생성
export function createComment(reviewId, token, payload) {
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}/comments`;
  const requestBody = JSON.stringify(payload);
  return http.post(url, requestBody, buildParams(token, 'review_comment_create_post'));
}

//리뷰 삭제
export function deleteReview(reviewId, token){
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}`;
  const params = buildParams(token, 'review_delete');
  return http.del(url, null, params);
}

/* =========================================================================
 * 좋아요, 신고 비즈니스 API
 * ========================================================================= */

// 리뷰 좋아요
export function toggleLike(reviewId, token) {
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}/likes`;
  return http.post(url, null, buildParams(token, 'review_likes_post'));
}

// 리뷰 신고 생성
export function createReport(reviewId, token, payload) {
  const url = `${BASE_URL}/api/v1/reviews/${reviewId}/reports`;
  const requestBody = JSON.stringify(payload);
  return http.post(url, requestBody, buildParams(token, 'review_reports_post'));
}

/* =========================================================================
 * 관리자(Admin) 전용 API
 * ========================================================================= */
// 신고 승인
export function approveReport(reportId, token) {
  const url = `${BASE_URL}/api/v1/admin/reviews/reports/${reportId}/approve`;
  return http.patch(url, null, buildParams(token, 'report_approve_patch'));
}

// 신고 반려
export function rejectReport(reportId, token) {
  const url = `${BASE_URL}/api/v1/admin/reviews/reports/${reportId}/reject`;
  return http.patch(url, null, buildParams(token, 'report_reject_patch'));
}