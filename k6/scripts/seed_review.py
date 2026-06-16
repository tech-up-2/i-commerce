import os
import psycopg2
from psycopg2.extras import execute_values


def get_connection():
  import os
  return psycopg2.connect(
    host=os.getenv("DB_HOST", "localhost"),
    port=int(os.getenv("DB_PORT", 5433)),
    database=os.getenv("DB_NAME", "i_commerce"),
    user=os.getenv("DB_USER", "postgres"),
    password=os.getenv("DB_PASSWORD", "1234")
  )

def seed_reviews():
    print("product_items(229~234) 매핑을 포함한 리뷰 데이터 적재 시작")

    target_product_id = 55

    option_names = [
      "사이즈: L / 색상: 핑크",
      "사이즈: L / 색상: 옐로우",
      "사이즈: XXL / 색상: 핑크",
      "사이즈: XXL / 색상: 옐로우",
      "사이즈: S / 색상: 핑크",
      "사이즈: S / 색상: 옐로우"
    ]

    conn = get_connection()
    cursor = conn.cursor()

    try:
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

      for i in range(1, 1001):
          user_id = (i % 609) + 1
          current_option = option_names[(i % len(option_names))]
          dummy_order_product_id = i
          star_rate = (i % 5) + 1

          if i == 1:
              star_rate = 5
              content = "★낙관적 락 & 페사드 동시성 테스트용 황금 리뷰★"
          else:
              content = f"[{current_option}] 평점 {star_rate}점 다중 검색 및 페이징 성능 테스트용 dummy 리뷰 {i}번"

          review_data.append((
              i, target_product_id, dummy_order_product_id, user_id, current_option,
              content, star_rate, 0, 0, 0, 'ACTIVE', False, False, False,
              '2026-06-16 11:15:00', '2026-06-16 11:15:00'
          ))

      execute_values(cursor, sql, review_data)
      conn.commit()
      print("제약조건 우회 완료, 리뷰 데이터 1,000건 적재 성공")

      cursor.execute("SELECT setval(pg_get_serial_sequence('reviews', 'id'), COALESCE((SELECT MAX(id) FROM reviews), 1));")
      conn.commit()
      print("리뷰 테이블 ID 시퀀스 동기화 완료")

    except Exception as e:
        print(f"❌ 에러 발생: {e}")
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