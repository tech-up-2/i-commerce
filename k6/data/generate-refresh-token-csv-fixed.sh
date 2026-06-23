#!/usr/bin/env bash

set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASSWORD="${PASSWORD:-password123!}"
OUTPUT_FILE="${OUTPUT_FILE:-k6/data/dummy-refresh-tokens.csv}"

MEMBER_COUNT="${MEMBER_COUNT:-15000}"
PARALLEL="${PARALLEL:-16}"

mkdir -p "$(dirname "$OUTPUT_FILE")"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

TASK_FILE="$TMP_DIR/login_tasks.csv"
OUTPUT_TMP="$TMP_DIR/result.csv"
FAIL_LOG="$TMP_DIR/login_failures.log"

: > "$TASK_FILE"
: > "$OUTPUT_TMP"
: > "$FAIL_LOG"

echo "email,refreshtoken" > "$OUTPUT_FILE"

echo "리프레시 토큰 CSV 병렬 생성 시작"
echo "BASE_URL=$BASE_URL"
echo "OUTPUT_FILE=$OUTPUT_FILE"
echo "MEMBER_COUNT=$MEMBER_COUNT"
echo "PARALLEL=$PARALLEL"

extract_refresh_token_from_json() {
  local body_file="$1"

  jq -r '
    if type == "object" then
      (
        .refreshToken? //
        .refresh_token? //
        .refreshTokenValue? //
        .refresh_token_value? //
        (
          if (.data? | type) == "object" then
            (
              .data.refreshToken? //
              .data.refresh_token? //
              .data.refreshTokenValue? //
              .data.refresh_token_value?
            )
          else
            empty
          end
        ) //
        (
          if (.result? | type) == "object" then
            (
              .result.refreshToken? //
              .result.refresh_token? //
              .result.refreshTokenValue? //
              .result.refresh_token_value?
            )
          else
            empty
          end
        ) //
        empty
      )
    else
      empty
    end
  ' "$body_file" 2>/dev/null
}

extract_refresh_token_from_header() {
  local header_file="$1"

  awk '
    BEGIN { IGNORECASE = 1 }
    {
      line = $0
      gsub(/\r/, "", line)

      if (line ~ /^refreshToken:[[:space:]]*/) {
        sub(/^refreshToken:[[:space:]]*/, "", line)
        print line
        exit
      }

      if (line ~ /^refresh_token:[[:space:]]*/) {
        sub(/^refresh_token:[[:space:]]*/, "", line)
        print line
        exit
      }

      if (line ~ /^Set-Cookie:[[:space:]]*/) {
        sub(/^Set-Cookie:[[:space:]]*/, "", line)
        split(line, parts, ";")
        split(parts[1], kv, "=")
        name = kv[1]
        value = kv[2]

        if (name == "refreshToken" || name == "refresh_token" || name == "REFRESH_TOKEN") {
          print value
          exit
        }
      }
    }
  ' "$header_file"
}

csv_escape() {
  local value="$1"
  value="${value//\"/\"\"}"
  printf '"%s"' "$value"
}

member_login_worker() {
  local task_no="$1"
  local email="$2"

  local header_file="$TMP_DIR/header_${task_no}.txt"
  local body_file="$TMP_DIR/body_${task_no}.json"
  local curl_error_file="$TMP_DIR/curl_error_${task_no}.log"

  local http_code
  http_code=$(curl -sS \
    -D "$header_file" \
    -o "$body_file" \
    -w "%{http_code}" \
    -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}" \
    2> "$curl_error_file")

  local refresh_token=""

  if [ -s "$body_file" ]; then
    refresh_token=$(extract_refresh_token_from_json "$body_file")
  fi

  if [ -z "$refresh_token" ] || [ "$refresh_token" = "null" ]; then
    refresh_token=$(extract_refresh_token_from_header "$header_file")
  fi

  if [ -z "$refresh_token" ] || [ "$refresh_token" = "null" ]; then
    {
      echo "[LOGIN_FAIL] task_no=$task_no email=$email http_code=$http_code"
      echo "--- curl_error ---"
      cat "$curl_error_file" 2>/dev/null || true
      echo "--- response_headers ---"
      cat "$header_file" 2>/dev/null || true
      echo "--- response_body ---"
      cat "$body_file" 2>/dev/null || true
      echo
    } >> "$FAIL_LOG"
    return
  fi

  printf '%s,%s,%s\n' "$task_no" "$(csv_escape "$email")" "$(csv_escape "$refresh_token")"
}

export BASE_URL
export PASSWORD
export TMP_DIR
export FAIL_LOG

export -f extract_refresh_token_from_json
export -f extract_refresh_token_from_header
export -f csv_escape
export -f member_login_worker

echo "로그인 대상 task 생성 시작"

for i in $(seq 1 "$MEMBER_COUNT"); do
  printf "%09d,activeMember%d@test.com\n" "$i" "$i" >> "$TASK_FILE"
done

echo "로그인 대상 task 생성 완료"
echo "총 로그인 시도 수: $(wc -l < "$TASK_FILE")"

echo "병렬 로그인 시작"

cat "$TASK_FILE" \
  | xargs -P "$PARALLEL" -I {} bash -c '
      IFS="," read -r task_no email <<< "{}"
      member_login_worker "$task_no" "$email"
    ' > "$OUTPUT_TMP"

echo "계정 순서대로 정렬 중"

sort -t ',' -k1,1n "$OUTPUT_TMP" | cut -d ',' -f2- >> "$OUTPUT_FILE"

echo "리프레시 토큰 CSV 생성 완료: $OUTPUT_FILE"
echo "생성된 리프레시 토큰 개수:"
tail -n +2 "$OUTPUT_FILE" | wc -l

echo "로그인 실패 수:"
wc -l < "$FAIL_LOG"

if [ "$(wc -l < "$FAIL_LOG")" -gt 0 ]; then
  FAIL_LOG_OUTPUT="$(dirname "$OUTPUT_FILE")/refresh-token-login-failures.log"
  cp "$FAIL_LOG" "$FAIL_LOG_OUTPUT"

  echo "로그인 실패 로그 저장 위치: $FAIL_LOG_OUTPUT"
  echo "로그인 실패 샘플:"
  sed -n '1,80p' "$FAIL_LOG_OUTPUT"
fi

echo "CSV 샘플:"
head -n 10 "$OUTPUT_FILE"
