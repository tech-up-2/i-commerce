#!/bin/bash

BASE_URL="http://localhost:8080"
PASSWORD="password123!"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-i_commerce}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-1234}"

extract_account_id_from_token() {
  local token="$1"

  local payload
  payload=$(echo "$token" | cut -d '.' -f2)

  local remainder=$(( ${#payload} % 4 ))

  if [ "$remainder" -eq 2 ]; then
    payload="${payload}=="
  elif [ "$remainder" -eq 3 ]; then
    payload="${payload}="
  elif [ "$remainder" -eq 1 ]; then
    payload="${payload}==="
  fi

  echo "$payload" \
    | tr '_-' '/+' \
    | base64 -d 2>/dev/null \
    | jq -r '.accountId // .sub // empty'
}

psql_query() {
  local query="$1"

  PGPASSWORD="$DB_PASSWORD" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -t \
    -A \
    -c "$query"
}

extract_token() {
  local response="$1"

  echo "$response" | jq -r '
    .accessToken //
    .token //
    .data.accessToken //
    .data.token //
    .result.accessToken //
    .result.token //
    empty
  '
}

extract_member_id() {
  local response="$1"

  echo "$response" | jq -r '
    .memberId //
    .data.memberId //
    .result.memberId //
    .id //
    .data.id //
    .result.id //
    empty
  '
}

extract_admin_id() {
  local response="$1"

  echo "$response" | jq -r '
    .adminId //
    .data.adminId //
    .result.adminId //
    .id //
    .data.id //
    .result.id //
    empty
  '
}

get_delivery_address_ids() {
  local user_id="$1"

  if [ -z "$user_id" ] || [ "$user_id" = "null" ]; then
    echo ""
    return
  fi

  psql_query "
    select coalesce(string_agg(id::text, '|' order by id), '')
    from delivery_addresses
    where user_id = $user_id
      and deleted_at is null;
  "
}

get_store_ids() {
  local seller_id="$1"

  if [ -z "$seller_id" ] || [ "$seller_id" = "null" ]; then
    echo ""
    return
  fi

  psql_query "
    select coalesce(string_agg(id::text, '|' order by id), '')
    from stores
    where seller_id = $seller_id
      and deleted_at is null;
  "
}

get_store_address_ids() {
  local seller_id="$1"

  if [ -z "$seller_id" ] || [ "$seller_id" = "null" ]; then
    echo ""
    return
  fi

  psql_query "
    select coalesce(string_agg(sa.id::text, '|' order by s.id, sa.id), '')
    from store_addresses sa
    join stores s on s.id = sa.store_id
    where s.seller_id = $seller_id
      and s.deleted_at is null
      and sa.deleted_at is null;
  "
}

check_member() {
  local email="activeMember1@test.com"

  response=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  token=$(extract_token "$response")
  member_id=$(extract_member_id "$response")
  delivery_address_ids=$(get_delivery_address_ids "$member_id")

  echo ""
  echo "===== 일반 회원 확인 ====="
  echo "email: $email"
  echo "memberId: $member_id"
  echo "deliveryAddressIds: $delivery_address_ids"
  echo "token exists: $([ -n "$token" ] && echo "YES" || echo "NO")"
  echo "csv row:"
  echo "$email,$PASSWORD,MEMBER,ACTIVE,CUSTOMER,$token,$member_id,$delivery_address_ids,,"
}

check_seller() {
  local email="approvedSeller1@test.com"

  response=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  token=$(extract_token "$response")
  member_id=$(extract_member_id "$response")
  store_ids=$(get_store_ids "$member_id")
  store_address_ids=$(get_store_address_ids "$member_id")

  echo ""
  echo "===== 판매자 확인 ====="
  echo "email: $email"
  echo "memberId/sellerId: $member_id"
  echo "storeIds: $store_ids"
  echo "storeAddressIds: $store_address_ids"
  echo "token exists: $([ -n "$token" ] && echo "YES" || echo "NO")"
  echo "csv row:"
  echo "$email,$PASSWORD,SELLER,APPROVED,SELLER,$token,$member_id,,$store_ids,$store_address_ids"
}

check_admin() {
  local email="activeMaster1@test.com"

  response=$(curl -s -X POST "$BASE_URL/api/v1/admin/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  token=$(extract_token "$response")
  admin_id=$(extract_admin_id "$response")

  if [ -z "$admin_id" ] || [ "$admin_id" = "null" ]; then
    admin_id=$(extract_account_id_from_token "$token")
  fi

  echo ""
  echo "===== 관리자 확인 ====="
  echo "email: $email"
  echo "adminId: $admin_id"
  echo "token exists: $([ -n "$token" ] && echo "YES" || echo "NO")"
  echo "csv row:"
  echo "$email,$PASSWORD,ADMIN,ACTIVE,MASTER,$token,$admin_id,,,"
}

echo "샘플 확인 시작"
echo "BASE_URL=$BASE_URL"
echo "DB=$DB_HOST:$DB_PORT/$DB_NAME"

check_member
check_seller
check_admin

echo ""
echo "샘플 확인 완료"