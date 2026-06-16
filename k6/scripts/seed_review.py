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
      print(f"데이터셋에 존재하는 진짜 상품 ID를 타겟으로 지정: {target_product_id}")
    else:
      target_product_id = 55
      print(f"[주의] 상품 테이블이 비어있어 기본값 {target_product_id}로 진행합니다.")

    cursor.execute("""
                   SELECT DISTINCT display_option_name
                   FROM product_items
                   WHERE product_id = %s AND display_option_name IS NOT NULL;
                   """, (target_product_id,))

    db_options = [r[0] for r in cursor.fetchall()]

    if not db_options:
      print("해당 상품은 옵션이 없는 상품(NONE)이므로 기본 텍스트 매핑을 사용합니다.")
      option_names = ["기본 옵션 / 단일 상품"]
    else:
      option_names = db_options
      print(f"[성공] 진짜 옵션 {len(option_names)}개 실시간 탐지 완료: {option_names}")

    print(f"\n해당 상품(ID: {target_product_id}) 기반 리뷰 1,000건 & 이미지 적재 시작")

    cursor.execute("SET session_replication_role = 'replica';")

    sql = """
          INSERT INTO reviews (
              id, product_id, order_product_id, user_id, display_option_name, content,
              star_rate, like_count, report_count, version, status,
              is_best, is_excluded, is_updated, created_at, updated_at
          ) VALUES %s
              ON CONFLICT (id) DO NOTHING;
          """

    review_data = []
    image_data = []
    image_id_counter = 1

    for i in range(1, 1001):
      user_id = (i % 609) + 1
      current_option = option_names[(i % len(option_names))]
      dummy_order_product_id = i
      star_rate = (i % 5) + 1

      if i == 1:
        star_rate = 5
        content = "★테스트용 리뷰★"
      else:
        content = f"[{current_option}] 평점 {star_rate}점 다중 검색 및 페이징 성능 테스트용 dummy 리뷰 {i}번"

      review_data.append((
        i, target_product_id, dummy_order_product_id, user_id, current_option,
        content, star_rate, 0, 0, 0, 'ACTIVE', False, False, False,
        '2026-06-16 11:15:00', '2026-06-16 11:15:00'
      ))

      image_count = random.randint(0, 10)

      for order in range(image_count):
        dummy_url = f"https://i-commerce.test.{i}_{order}.jpg"

        image_data.append((
          image_id_counter,
          dummy_url,
          order,
          i,
          '2026-06-16 11:15:00',
          '2026-06-16 11:15:00'
        ))
        image_id_counter += 1

    execute_values(cursor, sql, review_data)
    conn.commit()
    print("제약조건 우회 완료, 리뷰 데이터 1,000건 적재 성공")

    image_sql = """
                INSERT INTO review_images (id, image_url, sort_order, review_id, created_at, updated_at)
                VALUES %s ON CONFLICT (id) DO NOTHING;
                """

    execute_values(cursor, image_sql, image_data)
    conn.commit()
    print(f"리뷰 이미지 데이터 총 {len(image_data)}건 랜덤 분산 적재 완료")

    cursor.execute("SELECT setval(pg_get_serial_sequence('reviews', 'id'), COALESCE((SELECT MAX(id) FROM reviews), 1));")
    cursor.execute("SELECT setval(pg_get_serial_sequence('review_images', 'id'), COALESCE((SELECT MAX(id) FROM review_images), 1));")
    conn.commit()
    print("모든 테이블 ID 시퀀스 동기화 완료!")

  except Exception as e:
    print(f"에러 발생: {e}")
    conn.rollback()
  finally:
    try:
      cursor.execute("SET session_replication_role = 'origin';")
      conn.commit()
    except:
      pass
    cursor.close()
    conn.close()


if __name__ == "__main__":
  seed_reviews()