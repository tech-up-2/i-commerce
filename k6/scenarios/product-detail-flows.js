/**
 * Product Detail Scenarios
 *
 * 통합 실행: productDetailFullScenario(adminToken, sellerToken, storeId)
 *
 * 옵션 타입별 검증 전략:
 *   NONE   → optionGroups: [], lookupType: "NONE"
 *   SINGLE → optionGroups: [1개], lookupType: "SINGLE", singleMap 구조 검증
 *   DOUBLE → optionGroups: [2개], lookupType: "DOUBLE", doubleMap 구조 검증
 */
import { check, sleep } from 'k6';
import { getProductDetail } from '../domains/product/product-query-service.js';
import {
  setupProductTestData,
  cleanupProductTestData,
  createProductNoneFlow,
  createProductSingleFlow,
  createProductDoubleFlow,
} from './product-create-flows.js';

/**
 * 공통 응답 기본 구조 검증 헬퍼
 *
 * @param {Response} res
 * @param {number}   productId
 * @param {string}   label
 */
function checkBaseStructure(res, productId, label) {
  check(res, {
    [`[${label}] status is 200`]: (r) => r.status === 200,
    [`[${label}] code is SUCCESS`]: (r) => {
      try {
        return JSON.parse(r.body).code === 'SUCCESS';
      } catch {
        return false;
      }
    },
    [`[${label}] productId 일치`]: (r) => {
      try {
        return JSON.parse(r.body).data.productId === productId;
      } catch {
        return false;
      }
    },
    [`[${label}] selectedItem 존재`]: (r) => {
      try {
        const item = JSON.parse(r.body).data.selectedItem;
        return item !== null && typeof item.itemId === 'number';
      } catch {
        return false;
      }
    },
    [`[${label}] optionItemLookup 존재`]: (r) => {
      try {
        const lookup = JSON.parse(r.body).data.optionItemLookup;
        return lookup !== null && typeof lookup.lookupType === 'string';
      } catch {
        return false;
      }
    },
  });
}

/**
 * [케이스 1] NONE 타입 상품 상세 조회 플로우
 *
 * 검증 항목:
 *   - 기본 아이템 선택 (itemId 없이 조회)
 *   - optionGroups: 빈 배열
 *   - lookupType: "NONE"
 *   - singleMap, doubleMap: 빈 객체
 *
 * @param {number} productId - NONE 타입 상품 ID
 */
export function getProductDetailNoneFlow(productId) {
  console.log(`[Flow] NONE 타입 상품 상세 조회 | productId: ${productId}`);

  // itemId 없이 조회 → 기본 아이템(isDefault: true) 선택
  const res = getProductDetail(null, productId);

  console.log(`[Flow] getProductDetail (NONE) | status: ${res.status} | body: ${res.body}`);

  checkBaseStructure(res, productId, 'getProductDetail/NONE');

  check(res, {
    '[getProductDetail/NONE] optionType이 NONE': (r) => {
      try {
        return JSON.parse(r.body).data.optionType === 'NONE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/NONE] optionGroups가 빈 배열': (r) => {
      try {
        const groups = JSON.parse(r.body).data.optionGroups;
        return Array.isArray(groups) && groups.length === 0;
      } catch {
        return false;
      }
    },
    '[getProductDetail/NONE] lookupType이 NONE': (r) => {
      try {
        return JSON.parse(r.body).data.optionItemLookup.lookupType === 'NONE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/NONE] singleMap이 빈 객체': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.singleMap;
        return typeof map === 'object' && Object.keys(map).length === 0;
      } catch {
        return false;
      }
    },
    '[getProductDetail/NONE] doubleMap이 빈 객체': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.doubleMap;
        return typeof map === 'object' && Object.keys(map).length === 0;
      } catch {
        return false;
      }
    },
    '[getProductDetail/NONE] selectedItem에 attributes 존재': (r) => {
      try {
        const attrs = JSON.parse(r.body).data.selectedItem.attributes;
        return Array.isArray(attrs);
      } catch {
        return false;
      }
    },
  });

  sleep(0.5);
}

/**
 * [케이스 2] SINGLE 타입 상품 상세 조회 플로우
 *
 * 검증 항목:
 *   - 기본 아이템 선택 (itemId 없이 조회)
 *   - optionGroups: 1개
 *   - lookupType: "SINGLE"
 *   - singleMap 구조 검증 (optionValueId → itemId 매핑)
 *   - selected/available 필드 검증
 *   - singleMap에서 다른 itemId 추출 후 itemId로 재조회 검증
 *
 * @param {number} productId - SINGLE 타입 상품 ID
 */
export function getProductDetailSingleFlow(productId) {
  console.log(`[Flow] SINGLE 타입 상품 상세 조회 | productId: ${productId}`);

  // ── 기본 아이템 조회 ──────────────────────────────────────────
  const res = getProductDetail(null, productId);

  console.log(`[Flow] getProductDetail (SINGLE) | status: ${res.status} | body: ${res.body}`);

  checkBaseStructure(res, productId, 'getProductDetail/SINGLE');

  check(res, {
    '[getProductDetail/SINGLE] optionType이 SINGLE': (r) => {
      try {
        return JSON.parse(r.body).data.optionType === 'SINGLE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] optionGroups가 1개': (r) => {
      try {
        return JSON.parse(r.body).data.optionGroups.length === 1;
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] optionGroups[0] 구조 검증 (optionName, optionOrder, values)': (r) => {
      try {
        const group = JSON.parse(r.body).data.optionGroups[0];
        return (
            typeof group.optionName === 'string' &&
            typeof group.optionOrder === 'number' &&
            Array.isArray(group.values) &&
            group.values.length > 0
        );
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] optionValues 구조 검증 (selected, available 포함)': (r) => {
      try {
        const values = JSON.parse(r.body).data.optionGroups[0].values;
        return values.every(
            (v) =>
                typeof v.optionValueId === 'number' &&
                typeof v.value === 'string' &&
                typeof v.selected === 'boolean' &&
                typeof v.available === 'boolean'
        );
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] 선택된 optionValue가 1개': (r) => {
      try {
        const values = JSON.parse(r.body).data.optionGroups[0].values;
        return values.filter((v) => v.selected).length === 1;
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] lookupType이 SINGLE': (r) => {
      try {
        return JSON.parse(r.body).data.optionItemLookup.lookupType === 'SINGLE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] singleMap이 비어있지 않음': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.singleMap;
        return typeof map === 'object' && Object.keys(map).length > 0;
      } catch {
        return false;
      }
    },
    '[getProductDetail/SINGLE] singleMap 값이 number (itemId)': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.singleMap;
        return Object.values(map).every((v) => typeof v === 'number');
      } catch {
        return false;
      }
    },
  });

  sleep(0.3);

  // itemId 지정 조회

  try {
    const data = JSON.parse(res.body).data;
    const currentItemId = data.selectedItem.itemId;
    const singleMap = data.optionItemLookup.singleMap;

    // singleMap에서 현재 선택된 아이템이 아닌 다른 itemId 추출
    const otherItemId = Object.values(singleMap)
    .find((itemId) => itemId !== currentItemId);

    if (otherItemId) {
      console.log(`[Flow] SINGLE 타입 itemId 지정 조회 | itemId: ${otherItemId}`);

      const itemRes = getProductDetail(null, productId, otherItemId);

      console.log(`[Flow] getProductDetail (SINGLE/itemId) | status: ${itemRes.status} | body: ${itemRes.body}`);

      check(itemRes, {
        '[getProductDetail/SINGLE/itemId] status is 200': (r) => r.status === 200,
        '[getProductDetail/SINGLE/itemId] selectedItem.itemId가 요청한 itemId와 일치': (r) => {
          try {
            return JSON.parse(r.body).data.selectedItem.itemId === otherItemId;
          } catch {
            return false;
          }
        },
        '[getProductDetail/SINGLE/itemId] 선택된 optionValue가 변경됨': (r) => {
          try {
            const values = JSON.parse(r.body).data.optionGroups[0].values;
            const selectedValue = values.find((v) => v.selected);
            // 이전 조회의 선택 optionValueId와 달라야 합니다.
            const prevSelected = data.optionGroups[0].values.find((v) => v.selected);
            return selectedValue.optionValueId !== prevSelected.optionValueId;
          } catch {
            return false;
          }
        },
      });
    } else {
      console.warn('[Flow] SINGLE 타입: 다른 itemId를 찾지 못했습니다. (아이템이 1개뿐일 수 있습니다.)');
    }
  } catch (e) {
    console.error(`[Flow] SINGLE 타입 itemId 파싱 실패: ${e}`);
  }

  sleep(0.5);
}

/**
 * [케이스 3] DOUBLE 타입 상품 상세 조회 플로우
 *
 * 검증 항목:
 *   - 기본 아이템 선택 (itemId 없이 조회)
 *   - optionGroups: 2개
 *   - lookupType: "DOUBLE"
 *   - doubleMap 구조 검증 (optionValue1Id → { optionValue2Id → itemId } 중첩 매핑)
 *   - selected 필드가 각 optionGroup에서 1개씩 존재
 *   - doubleMap에서 다른 itemId 추출 후 itemId로 재조회 검증
 *
 * @param {number} productId - DOUBLE 타입 상품 ID
 */
export function getProductDetailDoubleFlow(productId) {
  console.log(`[Flow] DOUBLE 타입 상품 상세 조회 | productId: ${productId}`);

  // ── 기본 아이템 조회 ──────────────────────────────────────────
  const res = getProductDetail(null, productId);

  console.log(`[Flow] getProductDetail (DOUBLE) | status: ${res.status} | body: ${res.body}`);

  checkBaseStructure(res, productId, 'getProductDetail/DOUBLE');

  check(res, {
    '[getProductDetail/DOUBLE] optionType이 DOUBLE': (r) => {
      try {
        return JSON.parse(r.body).data.optionType === 'DOUBLE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] optionGroups가 2개': (r) => {
      try {
        return JSON.parse(r.body).data.optionGroups.length === 2;
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] 각 optionGroup 구조 검증': (r) => {
      try {
        const groups = JSON.parse(r.body).data.optionGroups;
        return groups.every(
            (g) =>
                typeof g.optionName === 'string' &&
                typeof g.optionOrder === 'number' &&
                Array.isArray(g.values) &&
                g.values.length > 0
        );
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] 각 optionGroup에서 selected가 1개씩 존재': (r) => {
      try {
        const groups = JSON.parse(r.body).data.optionGroups;
        return groups.every(
            (g) => g.values.filter((v) => v.selected).length === 1
        );
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] lookupType이 DOUBLE': (r) => {
      try {
        return JSON.parse(r.body).data.optionItemLookup.lookupType === 'DOUBLE';
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] doubleMap이 비어있지 않음': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.doubleMap;
        return typeof map === 'object' && Object.keys(map).length > 0;
      } catch {
        return false;
      }
    },
    '[getProductDetail/DOUBLE] doubleMap 중첩 구조 검증 (value1 → value2 → itemId)': (r) => {
      try {
        const map = JSON.parse(r.body).data.optionItemLookup.doubleMap;
        // 모든 value1 키에 대해 value2 → itemId 매핑이 올바른지 검증
        return Object.values(map).every(
            (innerMap) =>
                typeof innerMap === 'object' &&
                Object.values(innerMap).every((itemId) => typeof itemId === 'number')
        );
      } catch {
        return false;
      }
    },
  });

  sleep(0.3);

  // itemId 지정 조회

  try {
    const data = JSON.parse(res.body).data;
    const currentItemId = data.selectedItem.itemId;
    const doubleMap = data.optionItemLookup.doubleMap;

    let otherItemId = null;

    outer: for (const innerMap of Object.values(doubleMap)) {
      for (const itemId of Object.values(innerMap)) {
        if (itemId !== currentItemId) {
          otherItemId = itemId;
          break outer;
        }
      }
    }

    if (otherItemId) {
      console.log(`[Flow] DOUBLE 타입 itemId 지정 조회 | itemId: ${otherItemId}`);

      const itemRes = getProductDetail(null, productId, otherItemId);

      console.log(`[Flow] getProductDetail (DOUBLE/itemId) | status: ${itemRes.status} | body: ${itemRes.body}`);

      check(itemRes, {
        '[getProductDetail/DOUBLE/itemId] status is 200': (r) => r.status === 200,
        '[getProductDetail/DOUBLE/itemId] selectedItem.itemId가 요청한 itemId와 일치': (r) => {
          try {
            return JSON.parse(r.body).data.selectedItem.itemId === otherItemId;
          } catch {
            return false;
          }
        },
        '[getProductDetail/DOUBLE/itemId] 각 optionGroup의 selected가 변경됨': (r) => {
          try {
            const newGroups = JSON.parse(r.body).data.optionGroups;
            // 두 그룹 모두 selected가 1개씩 있어야 합니다.
            return newGroups.every(
                (g) => g.values.filter((v) => v.selected).length === 1
            );
          } catch {
            return false;
          }
        },
      });
    } else {
      console.warn('[Flow] DOUBLE 타입: 다른 itemId를 찾지 못했습니다.');
    }
  } catch (e) {
    console.error(`[Flow] DOUBLE 타입 itemId 파싱 실패: ${e}`);
  }

  sleep(0.5);
}


// 통합 시나리오 (생성 → 조회 → 정리 한번에 실행)

/**
 * 상품 상세 조회 통합 시나리오
 *
 * 흐름:
 *   [사전 준비] 카테고리/옵션/속성 세팅
 *             → NONE / SINGLE / DOUBLE 타입 상품 생성
 *       ↓
 *   [조회] NONE / SINGLE / DOUBLE 타입 상품 상세 조회
 *       ↓
 *   [사후 정리] 카테고리/옵션 삭제
 *
 * @param {string} adminToken  - 관리자 토큰 (사전 준비)
 * @param {string} sellerToken - 판매자 토큰 (상품 생성용)
 * @param {number} storeId     - 스토어 ID
 */
export function productDetailFullScenario(adminToken, sellerToken, storeId) {

  // 사전 준비: 카테고리/옵션/속성 세팅
  console.log('[Scenario] 사전 준비 시작 (관리자 토큰)');

  const testData = setupProductTestData(adminToken);

  if (!testData) {
    console.error('[Scenario] 사전 준비 실패. 시나리오를 중단합니다.');
    return;
  }

  console.log('[Scenario] 사전 준비 완료');
  sleep(0.5);

  // 사전 준비: 옵션 타입별 상품 생성
  console.log('[Scenario] 옵션 타입별 상품 생성 (셀러 토큰)');

  const noneProductId = createProductNoneFlow(sellerToken, storeId, testData);
  sleep(0.3);

  const singleProductId = createProductSingleFlow(sellerToken, storeId, testData);
  sleep(0.3);

  const doubleProductId = createProductDoubleFlow(sellerToken, storeId, testData);
  sleep(0.5);

  if (!noneProductId || !singleProductId || !doubleProductId) {
    console.error('[Scenario] 상품 생성 실패. 시나리오를 중단합니다.');
    cleanupProductTestData(adminToken, testData);
    return;
  }

  console.log(
      `[Scenario] 상품 생성 완료\n` +
      `  NONE   productId: ${noneProductId}\n` +
      `  SINGLE productId: ${singleProductId}\n` +
      `  DOUBLE productId: ${doubleProductId}`
  );

  // 조회: NONE 타입
  console.log('\n[Scenario] ===== NONE 타입 상품 상세 조회 =====');
  getProductDetailNoneFlow(noneProductId);

  // 조회: SINGLE 타입
  console.log('\n[Scenario] ===== SINGLE 타입 상품 상세 조회 =====');
  getProductDetailSingleFlow(singleProductId);

  // 조회: DOUBLE 타입
  console.log('\n[Scenario] ===== DOUBLE 타입 상품 상세 조회 =====');
  getProductDetailDoubleFlow(doubleProductId);

  // 사후 정리
  console.log('\n[Scenario] 사후 정리 시작 (관리자 토큰)');
  cleanupProductTestData(adminToken, testData);
  console.log('[Scenario] 완료');
}