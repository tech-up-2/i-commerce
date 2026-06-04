import { check, sleep } from 'k6';
import * as reviewAPI from '../domains/review/review-service.js';
import { ENV } from '../config/env.js';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
    const authorToken = ENV.AUTHOR_TOKEN;
    const viewerToken = ENV.VIEWER_TOKEN;

    const reviewPayload = {
            starRate: 5,
            content: "배송도 빠르고 상품도 너무 좋습니다!"
    };

    const createRes = reviewAPI.createReview(ENV.TARGET_ORDER_PRODUCT_ID, authorToken, reviewPayload);
    check(createRes, {'POST /create is 201': (r) => r.status === 201 });

    const statsRes = reviewAPI.getReviewStats(ENV.TARGET_PRODUCT_ID);
    check(statsRes, {'GET /stats status is 200': (r) => r.status === 200 });
    sleep(1);

    const listRes = reviewAPI.viewList(ENV.TARGET_PRODUCT_ID);
    check(listRes, {'GET /reviews list status is 200': (r) => r.status ===200 });
    sleep(1);

    const newReviewId = createRes.json('data');
    const likeRes = reviewAPI.toggleLike(newReviewId, viewerToken);
    check(likeRes, {'POST /likes status is 200': (r) => r.status === 200 });
    sleep(1);

    const reportPayload = {
            reason: "SPAM",
            content: "부적절한 광고성 리뷰입니다."
    };
    const reportRes = reviewAPI.createReport(ENV.TARGET_REVIEW_ID, viewerToken, reportPayload);
    check(reportRes, { 'POST /reports status is 201': (r) => r.status === 201 });
    sleep(1);


    const approveRes = reviewAPI.approveReport(ENV.TARGET_REPORT_ID, adminToken);
    check(approveRes, { 'PATCH /admin/reports/approve status is 200': (r) => r.status === 200 });
    sleep(1);

/*
    const rejectRes = reviewAPI.rejectReport(ENV.TARGET_REPORT_ID, adminToken);
    check(rejectRes, { 'PATCH /admin/reports/reject status is 200': (r) => r.status === 200 });
    sleep(1);
*/

}