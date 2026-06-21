import os
import random
import psycopg2
from psycopg2.extras import execute_values

def get_connection():
  return psycopg2.connect(
    host=os.getenv("DB_HOST", "localhost"),
    port=int(os.getenv("DB_PORT", 5433)),
    database=os.getenv("DB_NAME", "i_commerce"),
    user=os.getenv("DB_USER", "postgres"),
    password=os.getenv("DB_PASSWORD", "1234")
  )

def seed_reviews():
  conn = get_connection()
  cursor = conn.cursor()

  try:
    print("[데이터 매핑] 실시간 상품 및 옵션 조회 중...")

    cursor.execute("SELECT id FROM products LIMIT 1;")
    row = cursor.fetchone()
    if row:
      target_product_id = row[0]
      print(f"[성공] 데이터셋에 존재하는 첫 번째 상품 ID 타겟 지정: {target_product_id}")
    else:
      target_product_id = 24003
      print(f"[주의] 상품 테이블이 비어있어 기본값 {target_product_id}로 진행합니다.")

    cursor.execute("""
                   SELECT DISTINCT display_option_name
                   FROM product_items
                   WHERE product_id = %s AND display_option_name IS NOT NULL;
                   """, (target_product_id,))

    db_options = [r[0] for r in cursor.fetchall()]
    option_names = db_options if db_options else ["기본 옵션 / 단일 상품"]

    print("\n[안전 장치] 기존 데이터 충돌 방지를 위한 최대 ID 탐색 중...")
    cursor.execute("SELECT COALESCE(MAX(id), 0) FROM reviews;")
    start_review_id = cursor.fetchone()[0] + 1

    cursor.execute("SELECT COALESCE(MAX(id), 0) FROM review_images;")
    image_id_counter = cursor.fetchone()[0] + 1
    print(f"-> 리뷰는 {start_review_id}번부터, 이미지는 {image_id_counter}번부터 안전하게 이어 적재합니다.")

    print(f"\n해당 상품(ID: {target_product_id}) 기반 대용량 리뷰 40,000건 적재 시작...")

    print("[보안 변경] 외래키 제약조건 임시 비활성화 (Replica 모드)")
    cursor.execute("SET session_replication_role = 'replica';")

    review_sql = """
                 INSERT INTO reviews (
                     id, product_id, order_product_id, user_id, display_option_name, content,
                     star_rate, like_count, report_count, version, status,
                     is_best, is_excluded, is_updated, created_at, updated_at
                 ) VALUES %s ON CONFLICT (id) DO NOTHING; 
                 """

    image_sql = """
                INSERT INTO review_images (id, image_url, sort_order, review_id, created_at, updated_at)
                VALUES %s ON CONFLICT (id) DO NOTHING; 
                """

    review_data = []
    image_data = []

    TOTAL_REVIEWS = 40000
    CHUNK_SIZE = 10000
    MAX_USER_ID = 40000

    for offset in range(TOTAL_REVIEWS):
      review_id = start_review_id + offset
      user_id = random.randint(1, MAX_USER_ID)
      current_option = option_names[(offset % len(option_names))]
      dummy_order_product_id = review_id
      star_rate = (offset % 5) + 1

      if offset == 0:
        content = "★테스트용 첫 번째 리뷰★"
      else:
        content = f"[{current_option}] 평점 {star_rate}점 다중 검색 및 페이징 성능 테스트용 dummy 리뷰 {review_id}번"

      review_data.append((
        review_id, target_product_id, dummy_order_product_id, user_id, current_option,
        content, star_rate, 0, 0, 0, 'ACTIVE', False, False, False,
        '2026-06-16 11:15:00', '2026-06-16 11:15:00'
      ))

      image_count = random.randint(0, 10)
      for order in range(image_count):
        dummy_url = f"https://i-commerce.test.{review_id}_{order}.jpg"
        image_data.append((
          image_id_counter, dummy_url, order, review_id, '2026-06-16 11:15:00', '2026-06-16 11:15:00'
        ))
        image_id_counter += 1

      if (offset + 1) % CHUNK_SIZE == 0:
        execute_values(cursor, review_sql, review_data)
        if image_data:
          execute_values(cursor, image_sql, image_data)
        conn.commit()
        print(f"   -> {offset + 1}건 적재 완료...")

        review_data.clear()
        image_data.clear()

    if review_data:
      execute_values(cursor, review_sql, review_data)
      if image_data:
        execute_values(cursor, image_sql, image_data)
      conn.commit()

    print(f"\n리뷰 데이터 총 {TOTAL_REVIEWS}건 최종 적재 성공!")

    print("모든 테이블 ID 시퀀스 동기화 진행 중...")
    cursor.execute("SELECT setval(pg_get_serial_sequence('reviews', 'id'), COALESCE((SELECT MAX(id) FROM reviews), 1));")
    cursor.execute("SELECT setval(pg_get_serial_sequence('review_images', 'id'), COALESCE((SELECT MAX(id) FROM review_images), 1));")
    conn.commit()
    print("시퀀스 동기화 완료!")

  except Exception as e:
    print(f"에러 발생: {e}")
    conn.rollback()
  finally:
    try:
      cursor.execute("SET session_replication_role = 'origin';")
      conn.commit()
    except Exception as rollback_error:
      print(f"Origin 모드 복구 실패: {rollback_error}")

    cursor.close()
    conn.close()

if __name__ == "__main__":
  seed_reviews()