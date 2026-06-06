/**
 * Product Search Scenarios
 *
 * 통합 실행: productSearchFullScenario(adminToken, sellerToken, storeId)
 *
 * 시나리오:
 *   [CASE 1] keyword 검색 (로그인)
 *   [CASE 2] keyword 검색 (비로그인)
 *   [CASE 3] 비로그인 2페이지 접근 → 403 + PRD-40301 검증
 *   [CASE 4] categoryId 필터 검색
 *   [CASE 5] 가격 범위 필터 검색
 *   [CASE 6] attributeIds 필터 검색
 *   [CASE 7] 정렬 타입별 검색 (PRICE_ASC, PRICE_DESC, LATEST)
 *   [CASE 8] keyword 없음 → sortType LATEST 자동 적용 검증
 *   [CASE 9] keyword 1글자 → validation 에러 검증
 *
 */
import { check, sleep } from 'k6';
import { searchProducts } from '../domains/product/product-query-service.js';
import {
  createTestProduct,
  cleanupTestProduct,
} from '../lib/product-helper.js';

/**
 * SliceResponse 공통 구조 검증 헬퍼
 *
 * @param {Response} res
 * @param {string}   label
 */
function checkSliceStructure(res, label) {
  check(res, {
    [`[${label}] status is 200`]: (r) => r.status === 200,
    [`[${label}] code is SUCCESS`]: (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    [`[${label}] SliceResponse 구조 검증`]: (r) => {
      try {
        const data = JSON.parse(r.body).data;
        return (
            Array.isArray(data.content) &&
            typeof data.sliceNumber === 'number' &&
            typeof data.numberOfElements === 'number' &&
            typeof data.size === 'number' &&
            typeof data.hasNext === 'boolean' &&
            typeof data.isFirst === 'boolean' &&
            typeof data.isLast === 'boolean'
        );
      } catch {
        return false;
      }
    },
  });
}

/**
 * ProductItemSearchResponse 항목 구조 검증 헬퍼
 *
 * @param {Response} res
 * @param {string}   label
 */
function checkSearchItemStructure(res, label) {
  check(res, {
    [`[${label}] content 항목 구조 검증`]: (r) => {
      try {
        const content = JSON.parse(r.body).data.content;

        if (content.length === 0) return true;

        return content.every(
            (item) =>
                typeof item.productItemId === 'number' &&
                typeof item.productId === 'number' &&
                typeof item.productName === 'string' &&
                typeof item.price === 'number' &&
                typeof item.itemStatus === 'string' &&
                typeof item.categoryName === 'string'
        );
      } catch {
        return false;
      }
    },
  });
}


// 단위 검색 플로우

/**
 * [CASE 1] keyword 검색 - 로그인 사용자
 *
 * @param {string} token
 * @param {string} keyword
 */
export function searchByKeywordAuthFlow(token, keyword) {
  console.log(`[Flow] keyword 검색 (로그인) | keyword: ${keyword}`);

  const res = searchProducts(token, { keyword, page: 0, size: 20 });

  console.log(`[Flow] searchProducts (keyword/auth) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/keyword/auth');
  checkSearchItemStructure(res, 'search/keyword/auth');

  check(res, {
    '[search/keyword/auth] isFirst가 true (첫 페이지)': (r) => {
      try {
        return JSON.parse(r.body).data.isFirst === true;
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

/**
 * [CASE 2] keyword 검색 - 비로그인 사용자 (page 0)
 *
 * @param {string} keyword
 */
export function searchByKeywordGuestFlow(keyword) {
  console.log(`[Flow] keyword 검색 (비로그인/page 0) | keyword: ${keyword}`);

  const res = searchProducts(null, { keyword, page: 0, size: 20 });

  console.log(`[Flow] searchProducts (keyword/guest) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/keyword/guest');
  checkSearchItemStructure(res, 'search/keyword/guest');

  sleep(0.5);
}

/**
 * [CASE 3] 비로그인 사용자 2페이지 접근 → 에러 검증
 * 기대 응답: 403 FORBIDDEN + code: PRD-40301
 *
 * @param {string} keyword
 */
export function searchGuestPageLimitFlow(keyword) {
  console.log(`[Flow] 비로그인 2페이지 접근 (에러 검증) | keyword: ${keyword}`);

  const res = searchProducts(null, { keyword, page: 1, size: 20 });

  console.log(`[Flow] searchProducts (guest/page 1) | status: ${res.status} | body: ${res.body}`);

  check(res, {
    '[search/guest/page limit] status is 403': (r) => r.status === 403,
    '[search/guest/page limit] code is PRD-40301': (r) => {
      try {
        return JSON.parse(r.body).code === 'PRD-40301';
      } catch {
        return false;
      }
    },
    '[search/guest/page limit] 에러 메시지 확인': (r) => {
      try {
        return (
            JSON.parse(r.body).message ===
            '비로그인 사용자는 더 이상 페이지를 조회할 수 없습니다.'
        );
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

/**
 * [CASE 4] categoryId 필터 검색
 *
 * @param {string|null} token
 * @param {number}      categoryId
 */
export function searchByCategoryFlow(token, categoryId) {
  console.log(`[Flow] categoryId 필터 검색 | categoryId: ${categoryId}`);

  const res = searchProducts(token, { categoryId, page: 0, size: 20 });

  console.log(`[Flow] searchProducts (category) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/category');
  checkSearchItemStructure(res, 'search/category');

  check(res, {
    '[search/category] content의 categoryName이 존재': (r) => {
      try {
        const content = JSON.parse(r.body).data.content;
        if (content.length === 0) return true;
        return content.every((item) => typeof item.categoryName === 'string');
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

/**
 * [CASE 5] 가격 범위 필터 검색
 *
 * @param {string|null} token
 * @param {number}      minPrice
 * @param {number}      maxPrice
 */
export function searchByPriceRangeFlow(token, minPrice, maxPrice) {
  console.log(`[Flow] 가격 범위 필터 검색 | minPrice: ${minPrice}, maxPrice: ${maxPrice}`);

  const res = searchProducts(token, { minPrice, maxPrice, page: 0, size: 20 });

  console.log(`[Flow] searchProducts (price range) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/price');
  checkSearchItemStructure(res, 'search/price');

  check(res, {
    '[search/price] content의 price가 범위 내': (r) => {
      try {
        const content = JSON.parse(r.body).data.content;
        if (content.length === 0) return true;
        return content.every(
            (item) => item.price >= minPrice && item.price <= maxPrice
        );
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

/**
 * [CASE 6] attributeIds 필터 검색
 *
 * @param {string|null} token
 * @param {number[]}    attributeIds
 */
export function searchByAttributesFlow(token, attributeIds) {
  console.log(`[Flow] attributeIds 필터 검색 | attributeIds: ${attributeIds}`);

  const res = searchProducts(token, { attributeIds, page: 0, size: 20 });

  console.log(`[Flow] searchProducts (attributes) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/attributes');
  checkSearchItemStructure(res, 'search/attributes');

  sleep(0.5);
}

/**
 * [CASE 7] 정렬 타입별 검색
 *
 * @param {string|null} token
 */
export function searchBySortTypeFlow(token) {
  const sortTypes = ['PRICE_ASC', 'PRICE_DESC', 'LATEST'];

  for (const sortType of sortTypes) {
    console.log(`[Flow] 정렬 타입 검색 | sortType: ${sortType}`);

    const res = searchProducts(token, { sortType, page: 0, size: 20 });

    console.log(`[Flow] searchProducts (sort/${sortType}) | status: ${res.status} | body: ${res.body}`);

    checkSliceStructure(res, `search/sort/${sortType}`);

    if (sortType === 'PRICE_ASC') {
      check(res, {
        '[search/sort/PRICE_ASC] price 오름차순 정렬': (r) => {
          try {
            const content = JSON.parse(r.body).data.content;
            if (content.length < 2) return true;
            for (let i = 1; i < content.length; i++) {
              if (content[i].price < content[i - 1].price) return false;
            }
            return true;
          } catch {
            return false;
          }
        },
      });
    }

    if (sortType === 'PRICE_DESC') {
      check(res, {
        '[search/sort/PRICE_DESC] price 내림차순 정렬': (r) => {
          try {
            const content = JSON.parse(r.body).data.content;
            if (content.length < 2) return true;
            for (let i = 1; i < content.length; i++) {
              if (content[i].price > content[i - 1].price) return false;
            }
            return true;
          } catch {
            return false;
          }
        },
      });
    }

    sleep(0.3);
  }

  sleep(0.5);
}

/**
 * [CASE 8] keyword 없음 → sortType LATEST 자동 적용
 *
 * @param {string|null} token
 */
export function searchWithoutKeywordFlow(token) {
  console.log('[Flow] keyword 없음 검색 (LATEST 자동 적용)');

  const res = searchProducts(token, { page: 0, size: 20 });

  console.log(`[Flow] searchProducts (no keyword) | status: ${res.status} | body: ${res.body}`);

  checkSliceStructure(res, 'search/no-keyword');
  checkSearchItemStructure(res, 'search/no-keyword');

  sleep(0.5);
}

/**
 * [CASE 9] keyword 1글자 → validation 에러 검증
 *
 * @param {string|null} token
 */
export function searchWithInvalidKeywordFlow(token) {
  console.log('[Flow] keyword 1글자 검색 (validation 에러 검증)');

  const res = searchProducts(token, { keyword: '가', page: 0, size: 20 });

  console.log(`[Flow] searchProducts (invalid keyword) | status: ${res.status} | body: ${res.body}`);

  check(res, {
    '[search/invalid-keyword] validation 에러 반환 (400)': (r) => r.status === 400,
    '[search/invalid-keyword] code가 SUCCESS가 아님': (r) => {
      try {
        return JSON.parse(r.body).code !== 'SUCCESS';
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}


/**
 * 상품 검색 통합 시나리오
 *
 * @param {string} adminToken
 * @param {string} sellerToken
 * @param {number} storeId
 */
export function productSearchFullScenario(adminToken, sellerToken, storeId) {

  // 사전 준비: 검색 대상 상품 생성
  console.log('[Scenario] 사전 준비 시작 (검색 대상 상품 생성)');

  const result = createTestProduct(adminToken, sellerToken, storeId, 'SINGLE');

  if (!result) {
    console.error('[Scenario] 사전 준비 실패. 시나리오를 중단합니다.');
    return;
  }

  const { testData } = result;
  const searchKeyword = '스모크테스트';

  console.log(`[Scenario] 사전 준비 완료 | searchKeyword: ${searchKeyword}`);

  sleep(0.3);

  // CASE 1: keyword 검색 (로그인)
  console.log('\n[Scenario] ===== 케이스 1: keyword 검색 (로그인) =====');
  searchByKeywordAuthFlow(sellerToken, searchKeyword);

  // CASE 2: keyword 검색 (비로그인)
  console.log('\n[Scenario] ===== 케이스 2: keyword 검색 (비로그인) =====');
  searchByKeywordGuestFlow(searchKeyword);

  // CASE 3: 비로그인 2페이지 접근
  console.log('\n[Scenario] ===== 케이스 3: 비로그인 2페이지 접근 (에러 검증) =====');
  searchGuestPageLimitFlow(searchKeyword);

  // CASE 4: categoryId 필터 검색
  console.log('\n[Scenario] ===== 케이스 4: categoryId 필터 검색 =====');
  searchByCategoryFlow(sellerToken, testData.categoryId);

  // CASE 5: 가격 범위 필터 검색
  console.log('\n[Scenario] ===== 케이스 5: 가격 범위 필터 검색 =====');
  searchByPriceRangeFlow(sellerToken, 5000, 15000);

  // CASE 6: attributeIds 필터 검색
  console.log('\n[Scenario] ===== 케이스 6: attributeIds 필터 검색 =====');
  searchByAttributesFlow(sellerToken, [testData.attributeId]);

  // CASE 7: 정렬 타입별 검색
  console.log('\n[Scenario] ===== 케이스 7: 정렬 타입별 검색 =====');
  searchBySortTypeFlow(sellerToken);

  // CASE 8: keyword 없음
  console.log('\n[Scenario] ===== 케이스 8: keyword 없음 (LATEST 자동 적용) =====');
  searchWithoutKeywordFlow(sellerToken);

  // CASE 9: keyword 1글자 validation 에러
  console.log('\n[Scenario] ===== 케이스 9: keyword 1글자 (validation 에러) =====');
  searchWithInvalidKeywordFlow(sellerToken);

  // 사후 정리
  console.log('\n[Scenario] 사후 정리 시작 (관리자 토큰)');
  cleanupTestProduct(adminToken, testData);
  console.log('[Scenario] 완료');
}