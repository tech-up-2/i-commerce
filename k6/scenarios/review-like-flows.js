import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';


const activeMemberTokens = new SharedArray('active_member_tokens', function () {
    const fileData = open('../data/dummy-tokens.csv');

    const results = papaparse.parse(fileData, { header: false }).data;
    const activeTokens = [];

    for (let i = 0; i < results.length; i++) {
        const row = results[i];

        if (!row || row.length < 6) continue;

        const accountStatus = row[3];
        const role = row[4];
        const tokenValue = row[5];

        if (accountStatus === 'ACTIVE' && role === 'CUSTOMER' && tokenValue) {
            activeTokens.push(tokenValue.trim());
        }
    }

    if (activeTokens.length === 0) {
        console.error("ACTIVE 이면서 CUSTOMER인 토큰을 단 하나도 찾지 못했습니다. CSV 구조를 확인해주세요!");
    } else {
        console.log(`[성공] 총 ${activeTokens.length}개의 ACTIVE + CUSTOMER 유저 토큰 준비.`);
    }

    return activeTokens;
});

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function reviewLikeFlow() {
    if (activeMemberTokens.length === 0) return;

    const tokenIndex = getRandomInt(0, activeMemberTokens.length - 1);
    const token = activeMemberTokens[tokenIndex];

    const targetHost = __ENV.TARGET_HOST || 'http://localhost:8080';
    const targetReviewId = 3;

    const url = `${targetHost}/api/v1/reviews/${targetReviewId}/likes`;
    const params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
        tags: { name: 'review_like' },
    };

    const res = http.post(url, null, params);

    if (res.status !== 200) {
        console.log(`에러 터짐! [Status: ${res.status}] | 서버가 준 답변: ${res.body}`);
    }

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(0.1);
}