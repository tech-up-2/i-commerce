import http from 'k6/http';
import { buildParams } from '../../lib/http-helper.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUCT_BASE_PATH = `${BASE_URL}/api/v1/products`;



/**
 * [GET] /api/v1/products/{productId}
 * 상품 상세 조회
 *
 * @param {string|null} token     - 인증 토큰 (인가 불필요)
 * @param {number}      productId - 조회할 상품 ID
 * @param {number|null} itemId    - 선택할 아이템 ID
 *                                  null이면 기본 아이템(isDefault: true) 선택
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": {
 *     "productId": 1,
 *     "name": "상품명",
 *     "status": "ON_SALE",
 *     "optionType": "NONE" | "SINGLE" | "DOUBLE",
 *     "selectedItem": { "itemId": 1, ... },
 *     "optionGroups": [...],
 *     "optionItemLookup": {
 *       "lookupType": "NONE" | "SINGLE" | "DOUBLE",
 *       "singleMap": {},
 *       "doubleMap": {}
 *     }
 *   }
 * }
 */
export function getProductDetail(token, productId, itemId = null) {
  const url = itemId !== null
      ? `${PRODUCT_BASE_PATH}/${productId}?itemId=${itemId}`
      : `${PRODUCT_BASE_PATH}/${productId}`;

  return http.get(url, buildParams(token, 'product_detail'));
}

/**
 * [GET] /api/v1/products/search
 * 상품 검색
 *
 * @param {string|null} token   - 인증 토큰
 *                                null이면 비로그인(guest) 검색 → page 0만 허용
 * @param {Object}      params  - SearchProductRequest + Pageable
 * @param {string}      [params.keyword]      - 검색어 (min 2글자)
 * @param {number}      [params.categoryId]   - 카테고리 ID (하위 카테고리 포함 검색)
 * @param {number}      [params.minPrice]     - 최소 가격 (min 0)
 * @param {number}      [params.maxPrice]     - 최대 가격 (min 0)
 * @param {number[]}    [params.attributeIds] - 속성 ID 목록
 * @param {string}      [params.sortType]     - 정렬 타입 (RELEVANCE|PRICE_ASC|PRICE_DESC|LATEST)
 * @param {number}      [params.page]         - 페이지 번호 (기본 0, guest는 0만 허용)
 * @param {number}      [params.size]         - 페이지 크기 (기본 20)
 * @param {string}      [personaName] - 호출한 페르소나 이름 (예: 'keyword_explorer')
 * @param {string}      [step]  - 호출한 단계 이름
 * @returns {Response} k6 Response 객체
 *
 * 응답 구조:
 * {
 *   "code": "SUCCESS",
 *   "data": {
 *     "content": [
 *       {
 *         "productItemId": 1,
 *         "productId": 1,
 *         "productName": "상품명",
 *         "displayOptionName": "빨강/S",
 *         "price": 10000,
 *         "mainImageUrl": "https://...",
 *         "itemStatus": "ON_SALE",
 *         "categoryName": "상의",
 *         "relevanceScore": 1.5
 *       }
 *     ],
 *     "sliceNumber": 0,
 *     "numberOfElements": 20,
 *     "size": 20,
 *     "hasNext": true,
 *     "isFirst": true,
 *     "isLast": false
 *   }
 * }
 *
 * sortType 자동 결정 규칙:
 *   keyword 없음 + sortType 없음 → LATEST 자동 적용
 *   keyword 있음 + sortType 없음 → RELEVANCE 유지
 */
export function searchProducts(
    token, params = {},
    personaName = 'unspecified',
    step = 'unspecified'
) {

  const queryParams = {};

  if (params.keyword !== undefined)    queryParams.keyword    = params.keyword;
  if (params.categoryId !== undefined) queryParams.categoryId = params.categoryId;
  if (params.minPrice !== undefined)   queryParams.minPrice   = params.minPrice;
  if (params.maxPrice !== undefined)   queryParams.maxPrice   = params.maxPrice;
  if (params.sortType !== undefined)   queryParams.sortType   = params.sortType;
  if (params.page !== undefined)       queryParams.page       = params.page;
  if (params.size !== undefined)       queryParams.size       = params.size;

  const attributePart = (params.attributeIds || [])
  .map((id) => `attributeIds=${id}`)
  .join('&');

  const basePart = Object.entries(queryParams)
  .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
  .join('&');

  const queryString = [basePart, attributePart]
  .filter(Boolean)
  .join('&');

  const url = queryString
      ? `${PRODUCT_BASE_PATH}/search?${queryString}`
      : `${PRODUCT_BASE_PATH}/search`;

  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  return http.get(url, {
    headers,
    tags: {name: 'product_search', persona: personaName, step: step},
  });

}