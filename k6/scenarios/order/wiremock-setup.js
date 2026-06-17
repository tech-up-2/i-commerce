import http from 'k6/http';

export function initPurchaseScenario() {
    const wiremockAdminUrl = 'http://localhost:8089/__admin/mappings';

    // 1. 기존 와이어목에 등록된 장애/지연 규칙 모두 초기화
    http.request('DELETE', wiremockAdminUrl);

    // 2. 100% 확률로 즉시 200 OK 성공을 반환하도록 규정 (지연시간 30ms로 현실적인 네트워크만 시뮬레이션)
    const res = http.post(wiremockAdminUrl, JSON.stringify({
        request: {
            method: 'POST',
            url: '/confirm'
        },
        response: {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
            // Handlebars 문법을 사용하여 요청 바디의 데이터를 응답에 그대로 매핑합니다.
            jsonBody: {
                paymentKey: "{{jsonPath request.body '$.paymentKey'}}",
                orderId: "{{jsonPath request.body '$.orderId'}}",
                totalAmount: "{{jsonPath request.body '$.amount'}}",
                status: "DONE",
                approvedAt: "{{now}}" // 현재 시간 동적 생성
            },
            fixedDelayMilliseconds: 30
        }
    }), { headers: { 'Content-Type': 'application/json' } });

    if (res.status === 201 || res.status === 200) {
        console.log("================ [WireMock] 정상 결제 모드로 주입 완료 ================");
    } else {
        console.error(`[WireMock 주입 실패] 상태코드: ${res.status}`);
    }
}

export function initTimeoutScenario() {
    const wiremockAdminUrl = 'http://localhost:8089/__admin/mappings';

    // 1. 기존 규칙 초기화
    http.request('DELETE', wiremockAdminUrl);

    // 2. 5초 지연(5000ms) 규칙 주입
    const res = http.post(wiremockAdminUrl, JSON.stringify({
        request: {
            method: 'POST',
            url: '/confirm'
        },
        response: {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
            jsonBody: {
                paymentKey: "{{jsonPath request.body '$.paymentKey'}}",
                orderId: "{{jsonPath request.body '$.orderId'}}",
                totalAmount: "{{jsonPath request.body '$.amount'}}",
                status: "DONE",
                approvedAt: "{{now}}"
            },
            // 핵심: 5초 뒤에 응답을 주도록 설정
            fixedDelayMilliseconds: 5000
        }
    }), { headers: { 'Content-Type': 'application/json' } });

    if (res.status === 201 || res.status === 200) {
        console.log("================ [WireMock] 5초 지연 장애 모드로 주입 완료 ================");
    } else {
        console.error(`[WireMock 주입 실패] 상태코드: ${res.status}`);
    }
}

export function initFailureScenario() {
    const wiremockAdminUrl = 'http://localhost:8089/__admin/mappings';

    // 1. 기존 규칙 초기화
    http.request('DELETE', wiremockAdminUrl);

    // 5단계의 상태 순환 구조로 80%(5번 중 4번) 실패율 구현
    const totalSteps = 5;
    const failureCount = 4; // 4번 실패, 1번 성공

    for (let i = 1; i <= totalSteps; i++) {
        const currentState = `state_${i}`;
        const nextState = `state_${(i % totalSteps) + 1}`;

        // 1~4번째는 400 에러, 5번째는 200 성공
        const isFailure = i <= failureCount;

        const mappingRule = {
            scenarioName: "PaymentRandomFailure",
            requiredScenarioState: i === 1 ? "Started" : currentState,
            newScenarioState: nextState,
            request: {
                method: 'POST',
                url: '/confirm'
            },
            response: {
                status: isFailure ? 400 : 200,
                headers: { 'Content-Type': 'application/json' },
                fixedDelayMilliseconds: 30,
                jsonBody: isFailure ? {
                    code: "PAYMENT_NOT_ENOUGH_BALANCE",
                    message: "잔액이 부족하여 결제에 실패했습니다.",
                    orderId: "{{jsonPath request.body '$.orderId'}}"
                } : {
                    paymentKey: "{{jsonPath request.body '$.paymentKey'}}",
                    orderId: "{{jsonPath request.body '$.orderId'}}",
                    totalAmount: "{{jsonPath request.body '$.amount'}}",
                    status: "DONE",
                    approvedAt: "{{now}}"
                }
            }
        };

        http.post(wiremockAdminUrl, JSON.stringify(mappingRule), {
            headers: { 'Content-Type': 'application/json' }
        });
    }

    console.log("================ [WireMock] 결제 실패율 80% 모드로 주입 완료 ================");
}