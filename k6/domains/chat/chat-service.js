import ws from 'k6/ws';
import { check } from 'k6';

// 1. [환경 변수 및 URL 설정]
// 터미널에서 주입된 TARGET_HOST가 없으면 로컬 호스트(http://localhost:8080)를 기본값으로 사용합니다.
const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

// 웹소켓 통신을 위해 프로토콜 앞자리를 http://에서 ws://로 변경하고, 백엔드 엔드포인트인 '/connect'를 붙입니다.
const WS_URL = BASE_URL.replace(/^http/, 'ws') + '/connect';



 //2. [공통 인가/태그 파라미터 빌더]
 // 웹소켓 핸드셰이크 요청 시 필요한 헤더(JWT 토큰)와 k6 성능 측정용 태그명을 바인딩합니다.

function buildParams(token, tagName) {
  const headers = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;// Spring Security 또는 인터셉터에서 검증할 토큰
  }

  return {
    headers,
    tags: {
      name: tagName,// k6 결과창에서 'chat_connect'라는 이름으로 묶어서 지표를 볼 수 있게 해줍니다.
    },
  };
}
        /* =========================================================================
         * 실시간 채팅(STOMP/WebScoket) 비즈니스 API
         * ========================================================================= */

// 채팅 서버 연결 및 STOMP 핸드셰이크 프로토콜 수립
        function createFrame(command, headers = {}, body = '') {
          let frame = '${command}\n';// 1. Connect, SEND을 사용하고 줄바꿈을 할 수 있다.

        }