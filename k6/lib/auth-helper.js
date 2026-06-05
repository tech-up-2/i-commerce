// 간단한 인증 헬퍼
// 현재는 환경변수 기반 토큰 반환을 제공하며, 필요 시 CSV 사용자 취득 및 로그인 로직으로 확장 가능
export function getAuthTokenFromEnv() {
  // 테스트 시에는 환경변수 TEST_TOKEN을 주입하는 것을 권장
  return __ENV.TEST_TOKEN || null;
}

export default { getAuthTokenFromEnv };

