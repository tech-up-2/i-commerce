
export function getStoreId() {
  const storeId = parseInt(__ENV.TEST_STORE_ID);

  if (!storeId || isNaN(storeId)) {
    console.error('[Store] storeId가 없습니다.');
    return null;
  }

  return storeId;

}

export default { getStoreId };