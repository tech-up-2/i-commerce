import http from 'k6/http';
import {loginBlockTest} from "../../../scenarios/member/userService.js";
import {loadMemberUsers} from "../../../domains/member/csv-loader.js";

const users = loadMemberUsers('../../../data/dummy-tokens.csv')

http.setResponseCallback(
    http.expectedStatuses(200, 401, 429)
);

export const options = {
  stages: [
    {duration: '120s', target: 350},
    {duration: "300s", target: 350},
    {duration: '60s', target: 0}
  ],
  thresholds: {
    http_req_failed: ['rate < 0.01'],
    http_req_duration: ['p(95) < 3000', 'p(99) < 5500']
  }
};

export default function () {
  const user = users[(__VU - 1) % users.length];

  loginBlockTest(user);
}