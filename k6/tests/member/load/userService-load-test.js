import {userInfoManageTest} from "../../../scenarios/member/userService";

export const options = {
  stages: [
    {duration: '30s', target: 100},
    {duration: '30s', target: 200},
    {duration: '90s', target: 200},
    {duration: '30s', target: 0}
  ],
  threshold: {
    http_req_failed: ['rate < 0.01'],
    http_req_duration: ['p(95) < 1000', 'p(99) < 2000']
  }
};

export default function () {
  userInfoManageTest(user);
}