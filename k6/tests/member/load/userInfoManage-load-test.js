import {userInfoManageTest} from "../../../scenarios/member/userService.js";
import {loadMemberUsers} from "../../../domains/member/csv-loader.js";

const users = loadMemberUsers('../../../data/dummy-tokens.csv')

export const options = {
  stages: [
    {duration: '30s', target: 100},
    {duration: '30s', target: 300},
    {duration: '90s', target: 300},
    {duration: '30s', target: 0}
  ],
  thresholds: {
    http_req_failed: ['rate < 0.01'],
    http_req_duration: ['p(95) < 3000', 'p(99) < 4500']
  }
};

export default function () {
  const user = users[(__VU - 1) % users.length];

  userInfoManageTest(user);
}