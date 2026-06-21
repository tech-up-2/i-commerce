import {login} from "../../domains/member/auth";
import {sleep} from "k6";
import {getMyInfo} from "../../domains/member/member";
import {getMyAddresses} from "../../domains/member/delivery";

export function userService(user) {
  const loginRes = login(user.email, user.password);

  const accessToken = loginRes.accessToken;

  if (!accessToken) {
    console.error(`accessToken 없음 | body=${loginRes.body}`);
    return;
  }

  sleep(1);
  getMyInfo(accessToken);

  sleep(1);
  getMyAddresses(accessToken);

  sleep(1);
}