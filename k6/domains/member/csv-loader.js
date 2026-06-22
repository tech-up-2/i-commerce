import {SharedArray} from 'k6/data';

export function loadMemberUsers(filePath) {
  return new SharedArray('member users', function () {
    return open(filePath)
    .split('\n')
    .slice(1)
    .filter((line) => line.trim() !== '')
    .map((line) => {
      const [
        email,
        password,
        type,
        status,
        role,
        token,
        memberId,
        deliveryAddressIds,
        storeIds,
        storeAddressIds,
      ] = line.split(',');

      return {
        email: email.trim(),
        password: password.trim(),
        type: type.trim(),
        status: status.trim(),
        role: role.trim(),
        token: token.trim(),
        memberId: Number(memberId.trim()),
        deliveryAddressIds: parseIds(deliveryAddressIds),
        storeIds: parseIds(storeIds),
        storeAddressIds: parseIds(storeAddressIds),
      };
    });
  });
}

function parseIds(value) {
  if (!value || value.trim() === '') {
    return [];
  }

  return value
  .trim()
  .split('|')
  .filter((id) => id !== '')
  .map((id) => Number(id));
}