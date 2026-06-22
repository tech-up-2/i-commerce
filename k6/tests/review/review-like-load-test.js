import { reviewLikeFlow } from '../../scenarios/review-like-flows.js';

export const options = {
    vus: 100,
    iterations: 100,
    thresholds: {
        'http_req_duration{name:review_like}': ['p(95)<400', 'p(99)<1000'],
    },
};

export default function () {
    reviewLikeFlow();
}