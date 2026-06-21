/**
 * k6/scenarios/product/search-personas.js
 *
 */
import { sleep, check } from 'k6';
import { searchProducts }    from '../domains/product/product-query-service.js';
import { getAuthTokenFromEnv } from '../lib/auth-helper.js';
import {
  randomItem,
  randomAttributeIds,
  KEYWORDS,
  LEAF_CATEGORY_IDS,
  SORT_TYPES,
} from '../data/search-params-pool.js';

// Think Time 상수
/**
 * RTT_THINK: 요청 간 Think Time (초)
 * SET_THINK: 세션 종료 Think Time (초)
 */
const RTT_THINK = 4;
const SET_THINK = 20;

// 페르소나 태그 상수
const PERSONA = {
  SIMPLE_SEARCH:     'simple_search',
  PAGED_SEARCH:      'paged_search',
  PRICE_SORT_SEARCH: 'price_sort_search',
  COMPLEX_SEARCH:    'complex_search',
  ATTRIBUTE_SEARCH:  'attribute_search',
  CATEGORY_SEARCH:   'category_search',
};

// Step 태그 상수
const STEP = {
  KEYWORD:         'keyword',
  PAGE_NEXT:       'page_next',
  SORT_CHANGE:     'sort_change',
  ATTR_ADD:        'attr_add',
  CATEGORY_CHANGE: 'category_change',
};

// 공통 유틸
function pickTwoDistinctCategories() {
  const first = randomItem(LEAF_CATEGORY_IDS);
  let second = randomItem(LEAF_CATEGORY_IDS);
  while (second === first) {
    second = randomItem(LEAF_CATEGORY_IDS);
  }
  return [first, second];
}

// 페르소나 A-1: 일반 검색
/**
 * 흐름: 키워드 검색 (1회)
 * 비율: 25%
 */
export function simpleSearchScenario(token) {
  const keyword = randomItem(KEYWORDS);

  const res = searchProducts(
      token,
      { keyword, page: 0, size: 20 },
      PERSONA.SIMPLE_SEARCH,
      STEP.KEYWORD,
  );

  check(res, { 'status 200': (r) => r.status === 200 });

  sleep(SET_THINK);
}

// 페르소나 A-2: 페이지 탐색
/**
 * 흐름: 키워드 검색 → 다음 페이지
 * 비율: 10% (회원 전용 - 비회원은 GUEST_MAX_PAGE=0 제한으로 수행 불가)
 */
export function pagedSearchScenario(token) {
  const keyword = randomItem(KEYWORDS);

  const res1 = searchProducts(
      token,
      { keyword, page: 0, size: 20 },
      PERSONA.PAGED_SEARCH,
      STEP.KEYWORD,
  );
  check(res1, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res2 = searchProducts(
      token,
      { keyword, page: 1, size: 20 },
      PERSONA.PAGED_SEARCH,
      STEP.PAGE_NEXT,
  );
  check(res2, { 'status 200': (r) => r.status === 200 });
  sleep(SET_THINK);
}

// 페르소나 B: 가격 비교
/**
 * 흐름: 키워드 검색 → 가격 정렬 변경
 * 비율: 20%
 */
export function priceSortSearchScenario(token) {
  const keyword   = randomItem(KEYWORDS);
  const priceSort = Math.random() < 0.5 ? 'PRICE_ASC' : 'PRICE_DESC';

  const res1 = searchProducts(
      token,
      { keyword, page: 0, size: 20 },
      PERSONA.PRICE_SORT_SEARCH,
      STEP.KEYWORD,
  );
  check(res1, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res2 = searchProducts(
      token,
      { keyword, sortType: priceSort, page: 0, size: 20 },
      PERSONA.PRICE_SORT_SEARCH,
      STEP.SORT_CHANGE,
  );
  check(res2, { 'status 200': (r) => r.status === 200 });
  sleep(SET_THINK);
}

// 페르소나 C: 통합 검색
/**
 * 흐름: 카테고리+키워드 검색 → 속성 추가 → 정렬 변경
 * 비율: 15%
 */
export function complexSearchScenario(token) {
  const keyword    = randomItem(KEYWORDS);
  const categoryId = randomItem(LEAF_CATEGORY_IDS);
  const [attr1]    = randomAttributeIds(1);
  const sortType   = randomItem(SORT_TYPES);

  const res1 = searchProducts(
      token,
      { keyword, categoryId, page: 0, size: 20 },
      PERSONA.COMPLEX_SEARCH,
      STEP.KEYWORD,
  );
  check(res1, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res2 = searchProducts(
      token,
      { keyword, categoryId, attributeIds: [attr1], page: 0, size: 20 },
      PERSONA.COMPLEX_SEARCH,
      STEP.ATTR_ADD,
  );
  check(res2, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res3 = searchProducts(
      token,
      { keyword, categoryId, attributeIds: [attr1], sortType, page: 0, size: 20 },
      PERSONA.COMPLEX_SEARCH,
      STEP.SORT_CHANGE,
  );
  check(res3, { 'status 200': (r) => r.status === 200 });
  sleep(SET_THINK);
}

// 페르소나 D: 속성 탐색
/**
 * 흐름: 키워드 검색 → 속성 1개 추가 → 속성 누적(2개)
 * 비율: 20%
 */
export function attributeSearchScenario(token) {
  const keyword        = randomItem(KEYWORDS);
  const [attr1, attr2] = randomAttributeIds(2);

  const res1 = searchProducts(
      token,
      { keyword, page: 0, size: 20 },
      PERSONA.ATTRIBUTE_SEARCH,
      STEP.KEYWORD,
  );
  check(res1, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res2 = searchProducts(
      token,
      { keyword, attributeIds: [attr1], page: 0, size: 20 },
      PERSONA.ATTRIBUTE_SEARCH,
      STEP.ATTR_ADD,
  );
  check(res2, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res3 = searchProducts(
      token,
      { keyword, attributeIds: [attr1, attr2], page: 0, size: 20 },
      PERSONA.ATTRIBUTE_SEARCH,
      STEP.ATTR_ADD,
  );
  check(res3, { 'status 200': (r) => r.status === 200 });
  sleep(SET_THINK);
}

// 페르소나 E: 카테고리 탐색
/**
 * 흐름: 카테고리+키워드 검색 → 카테고리 변경
 * 비율: 10%
 */
export function categorySearchScenario(token) {
  const keyword                    = randomItem(KEYWORDS);
  const [categoryId1, categoryId2] = pickTwoDistinctCategories();

  const res1 = searchProducts(
      token,
      { keyword, categoryId: categoryId1, page: 0, size: 20 },
      PERSONA.CATEGORY_SEARCH,
      STEP.KEYWORD,
  );
  check(res1, { 'status 200': (r) => r.status === 200 });
  sleep(RTT_THINK);

  const res2 = searchProducts(
      token,
      { keyword, categoryId: categoryId2, page: 0, size: 20 },
      PERSONA.CATEGORY_SEARCH,
      STEP.CATEGORY_CHANGE,
  );
  check(res2, { 'status 200': (r) => r.status === 200 });
  sleep(SET_THINK);
}

// 페르소나 비중표
const PERSONA_WEIGHTS = [
  { name: PERSONA.SIMPLE_SEARCH,     weight: 0.25, fn: simpleSearchScenario,    guestAllowed: true  },
  { name: PERSONA.PAGED_SEARCH,      weight: 0.10, fn: pagedSearchScenario,     guestAllowed: false },
  { name: PERSONA.PRICE_SORT_SEARCH, weight: 0.20, fn: priceSortSearchScenario, guestAllowed: true  },
  { name: PERSONA.COMPLEX_SEARCH,    weight: 0.15, fn: complexSearchScenario,   guestAllowed: true  },
  { name: PERSONA.ATTRIBUTE_SEARCH,  weight: 0.20, fn: attributeSearchScenario, guestAllowed: true  },
  { name: PERSONA.CATEGORY_SEARCH,   weight: 0.10, fn: categorySearchScenario,  guestAllowed: true  },
];

// 합계 검증: 파일 로드 시점에 즉시 오류 발생시켜 실수 방지
const totalWeight = PERSONA_WEIGHTS.reduce((sum, p) => sum + p.weight, 0);
if (Math.abs(totalWeight - 1.0) > 0.001) {
  throw new Error(`[search-personas] PERSONA_WEIGHTS 합계가 1.0이 아닙니다: ${totalWeight}`);
}

function pickPersona() {
  const r = Math.random();
  let cumulative = 0;
  for (const persona of PERSONA_WEIGHTS) {
    cumulative += persona.weight;
    if (r < cumulative) return persona;
  }
  return PERSONA_WEIGHTS[PERSONA_WEIGHTS.length - 1];
}

// 메인 진입점
export function runSearchPersona() {
  const persona = pickPersona();
  persona.fn(getAuthTokenFromEnv());
}