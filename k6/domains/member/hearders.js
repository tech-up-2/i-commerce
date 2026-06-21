export function getJsonHeaders() {
  return {
    headers: {
      'Content-Type': 'application/json',
    },
  };
}

export function getAuthHeaders(accessToken) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
  };
}