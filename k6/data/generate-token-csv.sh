#!/usr/bin/env bash

set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASSWORD="${PASSWORD:-password123!}"
OUTPUT_FILE="${OUTPUT_FILE:-k6/data/dummy-tokens.csv}"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-i_commerce}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-1234}"

MEMBER_COUNT="${MEMBER_COUNT:-15000}"
SELLER_COUNT="${SELLER_COUNT:-1000}"
ADMIN_COUNT="${ADMIN_COUNT:-100}"

# 병렬 로그인 수
# 처음에는 8~16 추천
PARALLEL="${PARALLEL:-16}"

mkdir -p k6/data

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

OUTPUT_TMP="$TMP_DIR/result.csv"
FAIL_LOG="$TMP_DIR/login_failures.log"

DELIVERY_MAP_FILE="$TMP_DIR/delivery_map.tsv"
STORE_MAP_FILE="$TMP_DIR/store_map.tsv"
STORE_ADDRESS_MAP_FILE="$TMP_DIR/store_address_map.tsv"
TASK_FILE="$TMP_DIR/login_tasks.csv"

echo "email,password,type,status,role,token,memberId,deliveryAddressIds,storeIds,storeAddressIds" > "$OUTPUT_FILE"
: > "$FAIL_LOG"

echo "토큰 CSV 병렬 생성 시작"
echo "BASE_URL=$BASE_URL"
echo "DB=$DB_HOST:$DB_PORT/$DB_NAME"
echo "OUTPUT_FILE=$OUTPUT_FILE"
echo "PARALLEL=$PARALLEL"

psql_query() {
  local query="$1"

  PGPASSWORD="$DB_PASSWORD" psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -t \
    -A \
    -F $'\t' \
    -c "$query"
}

echo "ID 매핑 데이터 로딩 시작"

psql_query "
  select
    user_id,
    coalesce(string_agg(id::text, '|' order by id), '')
  from delivery_addresses
  where deleted_at is null
  group by user_id;
" > "$DELIVERY_MAP_FILE"

psql_query "
  select
    seller_id,
    coalesce(string_agg(id::text, '|' order by id), '')
  from stores
  where deleted_at is null
  group by seller_id;
" > "$STORE_MAP_FILE"

psql_query "
  select
    s.seller_id,
    coalesce(string_agg(sa.id::text, '|' order by s.id, sa.id), '')
  from store_addresses sa
  join stores s on s.id = sa.store_id
  where s.deleted_at is null
    and sa.deleted_at is null
  group by s.seller_id;
" > "$STORE_ADDRESS_MAP_FILE"

echo "ID 매핑 데이터 로딩 완료"
echo "배송지 매핑 수: $(wc -l < "$DELIVERY_MAP_FILE")"
echo "상점 매핑 수: $(wc -l < "$STORE_MAP_FILE")"
echo "상점주소 매핑 수: $(wc -l < "$STORE_ADDRESS_MAP_FILE")"

extract_token() {
  local response="$1"

  echo "$response" | jq -r '
    if type == "object" then
      (
        .accessToken? //
        .token? //
        (
          if (.data? | type) == "object" then
            (.data.accessToken? // .data.token?)
          else
            empty
          end
        ) //
        (
          if (.result? | type) == "object" then
            (.result.accessToken? // .result.token?)
          else
            empty
          end
        ) //
        empty
      )
    else
      empty
    end
  ' 2>/dev/null
}

extract_member_id() {
  local response="$1"

  echo "$response" | jq -r '
    if type == "object" then
      (
        .memberId? //
        .id? //
        (
          if (.data? | type) == "object" then
            (.data.memberId? // .data.id?)
          else
            empty
          end
        ) //
        (
          if (.result? | type) == "object" then
            (.result.memberId? // .result.id?)
          else
            empty
          end
        ) //
        empty
      )
    else
      empty
    end
  ' 2>/dev/null
}

extract_admin_id() {
  local response="$1"

  echo "$response" | jq -r '
    if type == "object" then
      (
        .adminId? //
        .accountId? //
        .id? //
        (
          if (.data? | type) == "object" then
            (.data.adminId? // .data.accountId? // .data.id?)
          else
            empty
          end
        ) //
        (
          if (.result? | type) == "object" then
            (.result.adminId? // .result.accountId? // .result.id?)
          else
            empty
          end
        ) //
        empty
      )
    else
      empty
    end
  ' 2>/dev/null
}

extract_account_id_from_token() {
  local token="$1"

  if [ -z "$token" ] || [ "$token" = "null" ]; then
    echo ""
    return
  fi

  local payload
  payload="$(echo "$token" | cut -d '.' -f2)"

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
    | { base64 -d 2>/dev/null || base64 -D 2>/dev/null; } \
    | jq -r '.accountId // .sub // empty' 2>/dev/null
}

lookup_map() {
  local file="$1"
  local key="$2"

  if [ -z "$key" ] || [ "$key" = "null" ]; then
    echo ""
    return
  fi

  awk -F $'\t' -v key="$key" '$1 == key { print $2; exit }' "$file"
}

member_login_worker() {
  local task_no="$1"
  local email="$2"
  local type="$3"
  local status="$4"
  local role="$5"

  local response
  response=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  local token
  token=$(extract_token "$response")

  if [ -z "$token" ] || [ "$token" = "null" ]; then
    echo "[MEMBER_LOGIN_FAIL] task_no=$task_no email=$email type=$type status=$status role=$role response=$response" >> "$FAIL_LOG"
    return
  fi

  local member_id
  member_id=$(extract_member_id "$response")

  if [ -z "$member_id" ] || [ "$member_id" = "null" ]; then
    member_id=$(extract_account_id_from_token "$token")
  fi

  local delivery_address_ids=""
  local store_ids=""
  local store_address_ids=""

  if [ "$type" = "MEMBER" ]; then
    delivery_address_ids=$(lookup_map "$DELIVERY_MAP_FILE" "$member_id")
  fi

  if [ "$type" = "SELLER" ]; then
    store_ids=$(lookup_map "$STORE_MAP_FILE" "$member_id")
    store_address_ids=$(lookup_map "$STORE_ADDRESS_MAP_FILE" "$member_id")
  fi

  echo "$task_no,$email,$PASSWORD,$type,$status,$role,$token,$member_id,$delivery_address_ids,$store_ids,$store_address_ids"
}

admin_login_worker() {
  local task_no="$1"
  local email="$2"
  local status="$3"
  local role="$4"

  local response
  response=$(curl -s -X POST "$BASE_URL/api/v1/admin/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")

  local token
  token=$(extract_token "$response")

  if [ -z "$token" ] || [ "$token" = "null" ]; then
    echo "[ADMIN_LOGIN_FAIL] task_no=$task_no email=$email status=$status role=$role response=$response" >> "$FAIL_LOG"
    return
  fi

  local admin_id
  admin_id=$(extract_admin_id "$response")

  if [ -z "$admin_id" ] || [ "$admin_id" = "null" ]; then
    admin_id=$(extract_account_id_from_token "$token")
  fi

  echo "$task_no,$email,$PASSWORD,ADMIN,$status,$role,$token,$admin_id,,,"
}

export BASE_URL
export PASSWORD
export DELIVERY_MAP_FILE
export STORE_MAP_FILE
export STORE_ADDRESS_MAP_FILE
export FAIL_LOG

export -f extract_token
export -f extract_member_id
export -f extract_admin_id
export -f extract_account_id_from_token
export -f lookup_map
export -f member_login_worker
export -f admin_login_worker

echo "로그인 대상 task 생성 시작"

: > "$TASK_FILE"

task_no=0

# 일반 유저: ACTIVE, SUSPENDED만 로그인 시도
for status in active suspended; do
  upper_status=$(echo "$status" | tr '[:lower:]' '[:upper:]')

  for i in $(seq 1 "$MEMBER_COUNT"); do
    task_no=$((task_no + 1))
    email="${status}Member${i}@test.com"

    printf "%09d,MEMBER_LOGIN,%s,MEMBER,%s,CUSTOMER\n" \
      "$task_no" "$email" "$upper_status" >> "$TASK_FILE"
  done
done

# 판매자: PENDING, APPROVED, BLOCKED, WITHDRAW 전부 로그인 시도
# 판매자 계정은 memberStatus가 ACTIVE라면 로그인 자체는 성공해야 함
for status in pending approved blocked withdraw; do
  upper_status=$(echo "$status" | tr '[:lower:]' '[:upper:]')

  for i in $(seq 1 "$SELLER_COUNT"); do
    task_no=$((task_no + 1))
    email="${status}Seller${i}@test.com"

    printf "%09d,MEMBER_LOGIN,%s,SELLER,%s,SELLER\n" \
      "$task_no" "$email" "$upper_status" >> "$TASK_FILE"
  done
done

# 관리자: ACTIVE만 로그인 시도
for role in Master Admin Operator; do
  upper_role=$(echo "$role" | tr '[:lower:]' '[:upper:]')

  for i in $(seq 1 "$ADMIN_COUNT"); do
    task_no=$((task_no + 1))
    email="active${role}${i}@test.com"

    printf "%09d,ADMIN_LOGIN,%s,ADMIN,ACTIVE,%s\n" \
      "$task_no" "$email" "$upper_role" >> "$TASK_FILE"
  done
done

echo "로그인 대상 task 생성 완료"
echo "총 로그인 시도 수: $(wc -l < "$TASK_FILE")"

echo "병렬 로그인 시작"

cat "$TASK_FILE" \
  | xargs -P "$PARALLEL" -I {} bash -c '
      IFS="," read -r task_no login_type email type status role <<< "{}"

      if [ "$login_type" = "MEMBER_LOGIN" ]; then
        member_login_worker "$task_no" "$email" "$type" "$status" "$role"
      elif [ "$login_type" = "ADMIN_LOGIN" ]; then
        admin_login_worker "$task_no" "$email" "$status" "$role"
      fi
    ' > "$OUTPUT_TMP"

echo "계정 순서대로 정렬 중"

sort -t ',' -k1,1n "$OUTPUT_TMP" | cut -d ',' -f2- >> "$OUTPUT_FILE"

echo "토큰 CSV 생성 완료: $OUTPUT_FILE"
echo "생성된 토큰 개수:"
tail -n +2 "$OUTPUT_FILE" | wc -l

echo "로그인 실패 수:"
wc -l < "$FAIL_LOG"

if [ "$(wc -l < "$FAIL_LOG")" -gt 0 ]; then
  FAIL_LOG_OUTPUT="k6/data/login-failures.log"
  cp "$FAIL_LOG" "$FAIL_LOG_OUTPUT"

  echo "로그인 실패 로그 저장 위치: $FAIL_LOG_OUTPUT"
  echo "로그인 실패 샘플:"
  head -n 20 "$FAIL_LOG_OUTPUT"
fi

echo "CSV 샘플:"
head -n 10 "$OUTPUT_FILE"