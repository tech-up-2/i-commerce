import ws from 'k6/ws';
import { sleep } from 'k6';

const BASE_URL = __ENV.TARGET_HOST || 'http://localhost:8080';

// WebSocket 기반 채팅 도메인: 채팅방 진입, 메시지 전송/수신 로직
// 이 함수는 순수하게 연결/전송/수신을 수행하고 결과 객체를 반환합니다.
export function joinChatAndSend(roomId, token, message = 'hello') {
  const result = { joined: false, messageLatency: null, disconnected: false };
  const wsUrl = `${BASE_URL.replace(/^http/, 'ws')}/ws/chat/${roomId}`;
  const params = { headers: { Authorization: token ? `Bearer ${token}` : '' }, tags: { name: 'chat_join' } };

  try {
    ws.connect(wsUrl, params, function (socket) {
      socket.on('open', function () {
        result.joined = true;
        const sendTs = Date.now();
        // 메시지 전송
        socket.send(JSON.stringify({ type: 'message', text: message }));

        // 수신 처리
        socket.on('message', function (msg) {
          // 메시지 수신까지의 지연
          result.messageLatency = Date.now() - sendTs;
          // 한 번 메시지 받고 종료
          socket.close();
        });

        // 타임아웃 안전장치
        socket.setTimeout(function () {
          try { socket.close(); } catch (e) {}
        }, 2000);
      });

      socket.on('close', function () {
        result.disconnected = true;
      });

      socket.on('error', function () {
        result.disconnected = true;
      });

      // 잠깐 대기
      sleep(1);
    });
  } catch (e) {
    result.disconnected = true;
  }

  return result;
}

