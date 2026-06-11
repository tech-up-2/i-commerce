import { check, sleep } from 'k6';
import * as reviewAPI from '../domains/review/review-service.js';
import authHelper from '../lib/auth-helper.js';

export function flow() {
    const authorToken = authHelper.getAuthTokenFromEnv();
    const viewerToken = __ENV.VIEWER_TOKEN;
    const adminToken = authHelper.getAdminToken();

    if (!authorToken || !adminToken || !viewerToken) {
        console.error('[Auth] 필수 토큰이 주입되지 않았습니다. 명령어를 확인하세요!');
        return;
    }

    // 1. 리뷰 생성
    const reviewPayload = {
        starRate: 5,
        content: "배송도 빠르고 상품도 너무 좋습니다!"
    };
    const createRes = reviewAPI.createReview(__ENV.TARGET_ORDER_PRODUCT_ID || 1, authorToken, reviewPayload);
    check(createRes, {'POST /reviews is 201': (r) => r.status === 201 });

    const newReviewId = createRes.json().data;

    // 2. 리뷰 통계 조회
    const statsRes = reviewAPI.getReviewStats(__ENV.TARGET_PRODUCT_ID || 1);
    check(statsRes, {'GET /stats status is 200': (r) => r.status === 200 });
    sleep(0.5);

    // 3. 리뷰 목록 조회
    const listRes = reviewAPI.viewList(__ENV.TARGET_PRODUCT_ID || 1);
    check(listRes, {'GET /reviews list status is 200': (r) => r.status === 200 });
    sleep(0.5);

    // 4. 리뷰 좋아요
    const likeRes = reviewAPI.toggleLike(newReviewId, viewerToken);
    check(likeRes, {'POST /likes status is 200': (r) => r.status === 200 });
    sleep(0.5);

    // 5. 리뷰 답글 생성
    const commentPayload = { content: "도움이 됐어요" };
    const commentRes = reviewAPI.createComment(newReviewId, viewerToken, commentPayload);
    check(commentRes, { 'POST /comments status is 201': (r) => r.status === 201 });
    sleep(0.5);

    // 6. 리뷰 신고 생성

    const reportPayload = { reportType: "SPAM", reportReason: "부적절한 광고성 리뷰입니다." };
    const reportRes = reviewAPI.createReport(newReviewId, viewerToken, reportPayload);
    check(reportRes, { 'POST /reports status is 201': (r) => r.status === 201 });
    sleep(0.5);

    const newReportId = reportRes.json().data;
    const approveRes = reviewAPI.approveReport(newReportId, adminToken);
    check(approveRes, { 'PATCH /admin/reports/approve status is 200': (r) => r.status === 200 });
    sleep(0.5);
}