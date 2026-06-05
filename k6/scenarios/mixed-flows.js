import { check, sleep } from 'k6';
import { searchProducts } from '../domains/product/product-service.js';
import { createOrder, payOrder } from '../domains/order/order-service.js';
import { joinChatAndSend } from '../domains/chat/delivery-service.js';
import authHelper from '../lib/auth-helper.js';

// 메트릭은 사용되는 시나리오 파일이나 전역에서 정의 가능
import { wsDisconnectRate, messageDeliveryLatency } from '../lib/metrics.js';
// 💡 팁: 커스텀 메트릭(Trend, Rate)은 공통으로 쓰기 위해 lib/metrics.js 등에 빼두는 것이 깔끔합니다.

export function executeMixedLoadFlow() {
    const token = authHelper.getAuthTokenFromEnv && authHelper.getAuthTokenFromEnv();
    const r = Math.random();

    try {
        if (r < 0.75) {
            // 1. 검색 흐름
            const q = 'laptop';
            const res = searchProducts(q, { headers: { Authorization: token ? `Bearer ${token}` : '' } });
            check(res, { 'search 200': (r) => r && r.status === 200 });
            sleep(0.1);
        } else if (r < 0.95) {
            // 2. 구매 흐름: 생성 -> 결제
            const productRes = searchProducts('laptop', { headers: { Authorization: token ? `Bearer ${token}` : '' } });
            let productId = null;
            try {
                const body = productRes.json();
                if (Array.isArray(body) && body.length > 0) productId = body[0].id || null;
            } catch (e) {}

            // 주문 생성
            const orderPayload = { productId: productId || 'sample-id', qty: 1 };
            const orderRes = createOrder(orderPayload, { headers: { Authorization: token ? `Bearer ${token}` : '' } });
            check(orderRes, { 'order create 2xx': (r) => r && r.status >= 200 && r.status < 300 });

            // 결제
            const orderId = (() => {
                try { return orderRes.json().orderId || orderRes.json().id; } catch (e) { return null; }
            })();

            const payRes = payOrder(orderId || 'sample-order-id', { method: 'card', amount: 1000 }, { headers: { Authorization: token ? `Bearer ${token}` : '' } });
            check(payRes, { 'payment 200': (r) => r && r.status === 200 });

        } else {
            // 3. 채팅 흐름
            const roomId = 'delivery-room-1';
            const chatResult = joinChatAndSend(roomId, token, 'hello from load test');
            if (chatResult.messageLatency !== null) {
                messageDeliveryLatency.add(chatResult.messageLatency);
            }
            if (chatResult.disconnected) {
                wsDisconnectRate.add(1);
            } else {
                wsDisconnectRate.add(0);
            }
        }
    } catch (e) {
        // 예외 처리
    }

    sleep(0.05);
}
