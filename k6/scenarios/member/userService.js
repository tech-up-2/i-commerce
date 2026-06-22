import {check, sleep} from "k6";
import {failedLogin, login} from "../../domains/member/auth";
import {getMyInfo, updateMyInfo} from "../../domains/member/member";
import {getMyAddresses} from "../../domains/member/delivery";

export function userServiceTest(user) {
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

export function userInfoManageTest(user) {
  const beforeRes = getMyInfo(user.accessToken, {
    tags: {name: '회원정보조회_수정전'}
  });

  if (beforeRes.status !== 200) {
    return;
  }

  sleep(1);

  const updateRequest = createUpdateRequest(user);

  const updateRes = updateMyInfo(user.accessToken, updateRequest, {
    tags: {name: '회원정보수정'}
  });

  if (updateRes.status !== 200) {
    return;
  }

  sleep(1);

  const afterRes = getMyInfo(user.accessToken, {
    tags: {name: '회원정보조회_수정후'}
  });

  check(afterRes, {
    '회원정보 수정값 반영': (r) => {
      const body = r.json();

      const data = body.data;

      return (
          data.name === updateRequest.name &&
          data.phoneNumber === updateRequest.phoneNumber
      )
    }
  });

  sleep(1);
}

export function loginBlockTest(user) {
  const wrongPassword = 'wrongPassword123!';

  for (let i = 0; i < 5; i++) {
    failedLogin(user.email, wrongPassword, {
      tags: {name: '로그인실패누적'},
      expectedStatuses: [401, 429]
    });

    sleep(0.2);
  }

  const blockedRes = login(user.email, user.password, {
    tags: {name: '차단이후정상비밀번호로그인'},
    expectedStatuses: [429]
  });

  check(blockedRes, {
    '차단 응답 반환': (r) => r.status === 429
  });

  sleep(1);
}