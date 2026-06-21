/**
 * data/search-params-pool.js
 *
 * 검색 파라미터 후보
 *
 */

export const KEYWORDS = [
  '삼성', 'LG', '나이키', '아디다스', '유니클로', '소니', '다이슨', '애플',
  '이니스프리', '설화수', '노스페이스', '뉴발란스', 'MLB',
  '의류', '전자', '뷰티', '스포츠', '식품', '가전',
  '블랙', '화이트', '슬림', '프리미엄', '무선', '드라이핏',
  '노트북', '청바지', '샴푸', '운동화', '이어폰', '냉장고',
];

export const LEAF_CATEGORY_IDS = __ENV.LEAF_CATEGORY_IDS
    ? __ENV.LEAF_CATEGORY_IDS.split(',').map(id => parseInt(id.trim(), 10))
    : Array.from({ length: 40 }, (_, i) => i + 3);

export const SORT_TYPES = ['RELEVANCE', 'PRICE_ASC', 'PRICE_DESC', 'LATEST'];

// seed_product_v2.py의 ATTRIBUTE_POOL과 동기화된 ID 목록
export const ATTRIBUTE_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];

export const PRICE_RANGES = [
  { minPrice: 1000,   maxPrice: 10000  },
  { minPrice: 10000,  maxPrice: 30000  },
  { minPrice: 30000,  maxPrice: 100000 },
  { minPrice: 100000, maxPrice: 300000 },
  { minPrice: 300000, maxPrice: null   },
];

export function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export function randomAttributeIds(count) {
  const shuffled = [...ATTRIBUTE_IDS].sort(() => Math.random() - 0.5);
  return shuffled.slice(0, count);
}

export function randomPriceRange() {
  return PRICE_RANGES[Math.floor(Math.random() * PRICE_RANGES.length)];
}