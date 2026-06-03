// k6 성능 목표(Threshold) 정의 파일
// 이 파일은 `tests/load-test.js`에서 import 되어 k6 옵션의 thresholds에 연결됩니다.
// 각 metric 이름은 k6의 메트릭/태그 필터 문법을 따릅니다.

export default {
  // 전체 HTTP 실패율: 0.1% 미만
  'http_req_failed': ['rate<0.001'],

  // 상품 검색 및 조회
  'http_req_duration{tags.name:product_search}': ['p(95)<150', 'p(99)<300'],
  'http_req_duration{tags.name:product_get}': ['p(95)<150', 'p(99)<300'],

  // 주문 및 결제
  'http_req_duration{tags.name:order_create}': ['p(95)<500', 'p(99)<1000'],
  'http_req_duration{tags.name:order_payment}': ['p(95)<500', 'p(99)<1000'],
  // 결제 에러율은 사실상 0% (매우 엄격)
  'http_req_failed{tags.name:order_payment}': ['rate<0.00001'],

  // 리뷰 도메인
  'http_req_duration{tags.name:review_list}': ['p(95)<200'],
  'http_req_duration{tags.name:review_search}': ['p(95)<250', 'p(99)<500'],
  'http_req_duration{tags.name:review_create}': ['p(95)<800', 'p(99)<1500'],

  // 채팅 도메인 (WebSocket 관련 커스텀 메트릭은 load-test.js에서 생성)
  'http_req_duration{tags.name:chat_join}': ['p(95)<200'],

  // 커스텀 메트릭: WebSocket disconnect 비율 (부하중 Disconnect 발생률 1% 미만)
  // 이 메트릭은 tests/load-test.js에서 Rate('ws_disconnect_rate')로 생성되어 사용됩니다.
  'ws_disconnect_rate': ['rate<0.01'],

  // 메시지 전달 지연: p(99) < 200ms
  // 이 메트릭은 tests/load-test.js에서 Trend('message_delivery_latency')로 기록됩니다.
  'message_delivery_latency': ['p(99)<200']
};

