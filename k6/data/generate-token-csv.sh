#!/bin/bash

BASE_URL="http://localhost:8080"
PASSWORD="password123!"
OUTPUT_FILE="k6/data/dummy-tokens.csv"

# PostgreSQL 접속 정보
# 필요하면 실행할 때 환경변수로 덮어쓸 수 있음
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-i_commerce}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"

mkdir -p k6/data

echo "email,password,type,status,role,token,memberId,deliveryAddressIds,storeIds,storeAddressIds" > "$OUTPUT_FILE"

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

psql_query() {
  local query="$1"

  PGPASSWORD="$DB_PASSWORD" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -t \
    -A \
    -c "$query" 2>/dev/null
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
    select coalesce(string_agg(id::text, '|'
           order by id), '')
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
    select coalesce(string_agg(sa.id::text, '|'
           order by s.id, sa.id), '')
    from store_addresses sa
    join stores s on s.id = sa.store_id
    where s.seller_id = $seller_id
      and s.deleted_at is null
      and sa.deleted_at is null;
  "
}

write_row() {
  local email="$1"
  local password="$2"
  local type="$3"
  local status="$4"
  local role="$5"
  local token="$6"
  local member_id="$7"
  local delivery_address_ids="$8"
  local store_ids="$9"
  local store_address_ids="${10}"

  echo "$email,$password,$type,$status,$role,$token,$member_id,$delivery_address_ids,$store_ids,$store_address_ids" >> "$OUTPUT_FILE"
}

member_login_and_write() {
  local email="$1"
  local type="$2"
  local status="$3"
  local role="$4"

  response=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  token=$(extract_token "$response")
  member_id=$(extract_member_id "$response")

  if [ -n "$token" ] && [ "$token" != "null" ]; then
    delivery_address_ids=""
    store_ids=""
    store_address_ids=""

    if [ "$type" = "MEMBER" ]; then
      delivery_address_ids=$(get_delivery_address_ids "$member_id")
    fi

    if [ "$type" = "SELLER" ]; then
      # 현재 구조에서 seller id == member id 라는 전제
      store_ids=$(get_store_ids "$member_id")
      store_address_ids=$(get_store_address_ids "$member_id")
    fi

    write_row \
      "$email" \
      "$PASSWORD" \
      "$type" \
      "$status" \
      "$role" \
      "$token" \
      "$member_id" \
      "$delivery_address_ids" \
      "$store_ids" \
      "$store_address_ids"

    echo "토큰 발급 성공: $email"
  else
    echo "토큰 발급 실패: $email"
  fi
}

admin_login_and_write() {
  local email="$1"
  local status="$2"
  local role="$3"

  response=$(curl -s -X POST "$BASE_URL/api/v1/admin/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  token=$(extract_token "$response")
  admin_id=$(extract_admin_id "$response")

  if [ -z "$admin_id" ] || [ "$admin_id" = "null" ]; then
    admin_id=$(extract_account_id_from_token "$token")
  fi

  if [ -n "$token" ] && [ "$token" != "null" ]; then
    write_row \
      "$email" \
      "$PASSWORD" \
      "ADMIN" \
      "$status" \
      "$role" \
      "$token" \
      "$admin_id" \
      "" \
      "" \
      ""

    echo "관리자 토큰 발급 성공: $email"
  else
    echo "관리자 토큰 발급 실패: $email"
  fi
}

echo "회원 토큰 발급 시도 시작"

for status in active inactive suspended withdrawn; do
  upper_status=$(echo "$status" | tr '[:lower:]' '[:upper:]')

  for i in $(seq 1 15000); do
    email="${status}Member${i}@test.com"
    member_login_and_write "$email" "MEMBER" "$upper_status" "CUSTOMER"
  done
done

echo "판매자 토큰 발급 시도 시작"

for status in pending approved blocked withdraw; do
  upper_status=$(echo "$status" | tr '[:lower:]' '[:upper:]')

  for i in $(seq 1 1000); do
    email="${status}Seller${i}@test.com"
    member_login_and_write "$email" "SELLER" "$upper_status" "SELLER"
  done
done

echo "관리자 토큰 발급 시도 시작"

for status in active locked withdrawn; do
  upper_status=$(echo "$status" | tr '[:lower:]' '[:upper:]')

  for role in Master Admin Operator; do
    upper_role=$(echo "$role" | tr '[:lower:]' '[:upper:]')

    for i in $(seq 1 100); do
      email="${status}${role}${i}@test.com"
      admin_login_and_write "$email" "$upper_status" "$upper_role"
    done
  done
done

echo "토큰 CSV 생성 완료: $OUTPUT_FILE"
echo "생성된 토큰 개수:"
tail -n +2 "$OUTPUT_FILE" | wc -l