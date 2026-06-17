"""
검색 부하테스트용 상품 데이터 적재 스크립트
"""

import os
import uuid
import random
import time
import argparse
import psycopg2
import psycopg2.extras
from faker import Faker
from tqdm import tqdm

# ─────────────────────────────────────────────
# DB 설정 (환경변수 우선, 없으면 기본값)
# ─────────────────────────────────────────────
DB_CONFIG = {
  "host":     os.getenv("DB_HOST", "localhost"),
  "port":     int(os.getenv("DB_PORT", 5433)),
  "dbname":   os.getenv("DB_NAME", "i_commerce"),
  "user":     os.getenv("DB_USER", "postgres"),
  "password": os.getenv("DB_PASSWORD", "1234"),
}

fake = Faker("ko_KR")

CATEGORY_TREE = {
  "패션의류": {
    "weight": 0.28,
    "children": ["남성의류", "여성의류", "아동의류", "속옷/잠옷", "스포츠웨어"]
  },
  "전자기기": {
    "weight": 0.22,
    "children": ["스마트폰", "노트북", "태블릿", "이어폰/헤드폰", "카메라", "스마트워치"]
  },
  "생활가전": {
    "weight": 0.12,
    "children": ["청소기", "에어컨", "세탁기", "냉장고", "공기청정기"]
  },
  "뷰티": {
    "weight": 0.13,
    "children": ["스킨케어", "메이크업", "향수", "헤어케어", "남성뷰티"]
  },
  "스포츠/레저": {
    "weight": 0.10,
    "children": ["운동기구", "등산/캠핑", "수영", "자전거", "골프"]
  },
  "식품": {
    "weight": 0.08,
    "children": ["신선식품", "건강식품", "간편식", "음료", "과자/스낵"]
  },
  "도서/문구": {
    "weight": 0.04,
    "children": ["소설", "자기계발", "학습/참고서", "문구류", "아트용품"]
  },
  "완구/취미": {
    "weight": 0.03,
    "children": ["블록/퍼즐", "RC/드론", "보드게임", "피규어", "악기"]
  },
}

BRANDS = ["삼성", "LG", "나이키", "아디다스", "유니클로", "자라", "H&M", "뉴발란스",
          "MLB", "아이더", "노스페이스", "코닥", "소니", "파나소닉", "필립스",
          "다이슨", "샤오미", "애플", "이니스프리", "설화수", "라네즈", "에뛰드",
          "닥터자르트", "아모레", "스킨푸드", "미샤", "토니모리", "AHC"]

ADJECTIVES = ["프리미엄", "베이직", "슬림핏", "릴렉스핏", "오버핏", "모던", "클래식",
              "빈티지", "캐주얼", "포멀", "데일리", "스포티", "럭셔리", "에코", "울트라"]

COLORS = ["블랙", "화이트", "네이비", "그레이", "베이지", "카키", "브라운",
          "레드", "블루", "그린", "핑크", "퍼플", "옐로우", "오렌지"]

PATTERNS = ["체크", "스트라이프", "플레인", "플로럴", "도트"]  # 5개 → 별도 상수로 분리

MODELS = ["Pro", "Max", "Ultra", "Plus", "Lite", "Air", "Mini",
          "X1", "S23", "A52", "Z5", "GT9", "V40", "K30"]

SIZES = ["XS", "S", "M", "L", "XL", "XXL", "FREE"]

VOLUME_ML = ["50ml", "100ml", "150ml", "200ml", "300ml", "500ml"]

PRODUCT_NAME_TEMPLATES = {
  "남성의류":     ["[브랜드] 남성 [형용사] 반팔티", "[브랜드] 슬림핏 청바지", "[브랜드] 캐주얼 자켓"],
  "여성의류":     ["[브랜드] 여성 [형용사] 원피스", "[브랜드] 캐주얼 블라우스", "[브랜드] 슬랙스"],
  "아동의류":     ["아동용 [형용사] 티셔츠", "키즈 청바지 세트", "유아 바디수트"],
  "속옷/잠옷":    ["[브랜드] 남성 [형용사] 브리프 3팩", "[브랜드] 여성 파자마 세트"],
  "스포츠웨어":   ["[브랜드] 드라이핏 반팔", "[브랜드] 요가팬츠", "러닝 자켓"],
  "스마트폰":     ["[브랜드] [모델] 스마트폰", "[브랜드] [모델] Pro 5G"],
  "노트북":       ["[브랜드] [형용사] 노트북", "[브랜드] 울트라북"],
  "태블릿":       ["[브랜드] 태블릿", "[브랜드] 패드 프로"],
  "이어폰/헤드폰":["[브랜드] 무선 이어폰", "[브랜드] 노이즈캔슬링 헤드폰", "[브랜드] 블루투스 이어버드"],
  "카메라":       ["[브랜드] 미러리스 카메라 [모델]", "[브랜드] DSLR [형용사] 키트"],
  "스마트워치":   ["[브랜드] 스마트워치 [모델]", "[브랜드] 피트니스 밴드"],
  "청소기":       ["[브랜드] 무선 청소기", "[브랜드] 로봇청소기"],
  "에어컨":       ["[브랜드] 벽걸이 에어컨", "[브랜드] 스탠드형 에어컨"],
  "세탁기":       ["[브랜드] 드럼세탁기", "[브랜드] 통돌이 세탁기"],
  "냉장고":       ["[브랜드] 양문형 냉장고", "[브랜드] 미니 냉장고"],
  "공기청정기":   ["[브랜드] 공기청정기 [모델]", "[브랜드] 차량용 공기청정기"],
  "스킨케어":     ["[브랜드] [형용사] 토너", "[브랜드] 수분크림", "[브랜드] 선크림 SPF50+"],
  "메이크업":     ["[브랜드] 파운데이션", "[브랜드] 립스틱", "[브랜드] 마스카라"],
  "향수":         ["[브랜드] 오드퍼퓸", "[브랜드] 바디미스트"],
  "헤어케어":     ["[브랜드] [형용사] 샴푸", "[브랜드] 헤어에센스", "[브랜드] 드라이어"],
  "남성뷰티":     ["[브랜드] 남성 올인원", "[브랜드] 면도기 세트"],
  "운동기구":     ["[브랜드] 덤벨 세트", "[브랜드] 요가매트", "풀업바"],
  "등산/캠핑":    ["[브랜드] 등산화", "[브랜드] 텐트", "배낭"],
  "수영":         ["[브랜드] 수영복", "[브랜드] 수경"],
  "자전거":       ["[브랜드] 로드바이크", "[브랜드] 전동킥보드"],
  "골프":         ["[브랜드] 골프채 세트", "[브랜드] 골프공"],
  "신선식품":     ["국내산 [지역] [식품]", "제철 [식품] 선물세트"],
  "건강식품":     ["[브랜드] 유산균", "[브랜드] 오메가3"],
  "간편식":       ["[브랜드] 즉석밥", "[브랜드] 국/찌개 간편식"],
  "음료":         ["[브랜드] [음료종류]", "[브랜드] 원두커피"],
  "과자/스낵":    ["[브랜드] [과자이름]", "[브랜드] 젤리"],
  "소설":         ["소설 [제목]", "베스트셀러 소설"],
  "자기계발":     ["자기계발서 [제목]", "성공의 법칙 시리즈"],
  "학습/참고서":  ["[학년] [과목] 참고서", "수능 [과목] 완성"],
  "문구류":       ["[브랜드] 볼펜 세트", "[브랜드] 노트"],
  "아트용품":     ["[브랜드] 수채화 물감 세트", "[브랜드] 스케치북"],
  "블록/퍼즐":    ["[브랜드] 블록 세트", "[브랜드] 3D 퍼즐"],
  "RC/드론":      ["[브랜드] 드론 [모델]", "[브랜드] RC카"],
  "보드게임":     ["[게임이름] 보드게임", "카드게임 [이름]"],
  "피규어":       ["[캐릭터] 피규어", "[브랜드] 아트토이"],
  "악기":         ["[브랜드] 어쿠스틱 기타", "[브랜드] 디지털 피아노"],
}


def random_product_name(sub_category: str) -> str:
  templates = PRODUCT_NAME_TEMPLATES.get(sub_category, ["[브랜드] [형용사] 상품"])
  tpl = random.choice(templates)
  result = tpl
  result = result.replace("[브랜드]", random.choice(BRANDS))
  result = result.replace("[형용사]", random.choice(ADJECTIVES))
  result = result.replace("[모델]", random.choice(MODELS))
  result = result.replace("[지역]", random.choice(["경상도", "전라도", "충청도", "강원도", "제주"]))
  result = result.replace("[식품]", random.choice(["딸기", "사과", "한우", "삼겹살", "갈치", "고등어", "참외"]))
  result = result.replace("[음료종류]", random.choice(["제로콜라", "에너지드링크", "아이스티", "탄산수", "과일주스"]))
  result = result.replace("[과자이름]", random.choice(["허니버터칩", "새우깡", "포카칩", "꼬깔콘", "빼빼로"]))
  result = result.replace("[게임이름]", random.choice(["카탄", "티켓투라이드", "스플렌더", "루미큐브", "블로커스"]))
  result = result.replace("[이름]", random.choice(["우노", "원카드", "고피쉬", "세트", "도블"]))
  result = result.replace("[캐릭터]", random.choice(["건담", "원피스", "진격의 거인", "토토로"]))
  result = result.replace("[제목]", fake.catch_phrase()[:20])
  result = result.replace("[학년]", random.choice(["중1", "중2", "중3", "고1", "고2", "고3"]))
  result = result.replace("[과목]", random.choice(["수학", "영어", "국어", "과학", "사회"]))
  return result


def random_price() -> int:
  price = int(random.lognormvariate(10.7, 0.8))
  price = max(1000, min(price, 5_000_000))
  return (price // 100) * 100


ATTRIBUTE_POOL = [
  (1,  ["색상: 블랙", "색상: 화이트", "색상: 네이비", "색상: 그레이", "색상: 베이지",
        "색상: 레드", "색상: 블루", "색상: 그린", "색상: 핑크", "색상: 옐로우"]),
  (2,  ["사이즈: XS", "사이즈: S", "사이즈: M", "사이즈: L", "사이즈: XL", "사이즈: XXL"]),
  (3,  ["소재: 면", "소재: 폴리에스터", "소재: 울", "소재: 린넨", "소재: 나일론", "소재: 레이온"]),
  (4,  ["성별: 남성", "성별: 여성", "성별: 공용", "성별: 아동"]),
  (5,  ["계절: 봄/여름", "계절: 가을/겨울", "계절: 사계절"]),
  (6,  ["원산지: 국내산", "원산지: 중국산", "원산지: 베트남산", "원산지: 이탈리아산"]),
  (7,  ["배터리: 없음", "배터리: 내장형", "배터리: AA 건전지", "배터리: AAA 건전지"]),
  (8,  ["연결방식: 유선", "연결방식: 무선(블루투스)", "연결방식: 무선(WiFi)", "연결방식: USB-C"]),
  (9,  ["피부타입: 지성", "피부타입: 건성", "피부타입: 복합성", "피부타입: 민감성", "피부타입: 중성"]),
  (10, ["향: 무향", "향: 플로럴", "향: 시트러스", "향: 우디", "향: 허브"]),
  (11, ["용량: 50ml", "용량: 100ml", "용량: 150ml", "용량: 200ml", "용량: 300ml", "용량: 500ml"]),
  (12, ["인증: KC인증", "인증: CE인증", "인증: FDA승인", "인증: 유기농인증"]),
  (13, ["무게: 100g 미만", "무게: 100~300g", "무게: 300~500g", "무게: 500g~1kg", "무게: 1kg 이상"]),
  (14, ["AS보증: 1년", "AS보증: 2년", "AS보증: 3년", "AS보증: 없음"]),
  (15, ["포장: 일반포장", "포장: 선물포장 가능", "포장: 에코포장"]),
]


def pick_attributes_for_item(n_attrs: int) -> list:
  chosen = random.sample(ATTRIBUTE_POOL, min(n_attrs, len(ATTRIBUTE_POOL)))
  result = []
  for order, (attr_id, names) in enumerate(chosen):
    result.append((attr_id, random.choice(names), order))
  return result


def derive_item_status(product_status: str) -> str:
  if product_status == "DISCONTINUED":
    return "OFF_SALE"
  elif product_status == "PENDING":
    return random.choices(["OFF_SALE", "OUT_OF_STOCK"], weights=[0.7, 0.3])[0]
  else:
    return random.choices(
      ["ON_SALE", "OFF_SALE", "OUT_OF_STOCK"],
      weights=[0.80, 0.05, 0.15]
    )[0]


def _add_item_meta(item_rows_meta, pid, opt_type, p_status,
    n_option1=0, n_option2=0,
    ov1_vals=None, ov2_vals=None,
    opt1_name=None, opt2_name=None):
  """item_rows_meta에 메타 정보 추가 (실제 INSERT는 나중에 일괄 처리)"""

  if opt_type == "NONE":
    base_price = random_price()
    sku = f"SKU-{uuid.uuid4().hex[:12].upper()}"
    item_status = derive_item_status(p_status)
    item_rows_meta.append({
      "pid": pid,
      "sku": sku,
      "price": base_price,
      "main_img": f"https://cdn.example.com/items/{sku}/main.jpg",
      "item_status": item_status,
      "display_option": None,
      "is_default": True,
      "n_attrs": random.randint(1, 5),
    })

  elif opt_type == "SINGLE":
    base_price = random_price()
    is_first = True
    for val in (ov1_vals or []):
      sku = f"SKU-{uuid.uuid4().hex[:12].upper()}"
      item_status = derive_item_status(p_status)
      price_adj = int(base_price * random.uniform(0.8, 1.3))
      item_rows_meta.append({
        "pid": pid,
        "sku": sku,
        "price": price_adj,
        "main_img": f"https://cdn.example.com/items/{sku}/main.jpg",
        "item_status": item_status,
        "display_option": f"{opt1_name}: {val}",
        "is_default": is_first,
        "n_attrs": random.randint(1, 5),
      })
      is_first = False

  elif opt_type == "DOUBLE":
    base_price = random_price()
    is_first = True
    for v1 in (ov1_vals or []):
      for v2 in (ov2_vals or []):
        sku = f"SKU-{uuid.uuid4().hex[:12].upper()}"
        item_status = derive_item_status(p_status)
        price_adj = int(base_price * random.uniform(0.8, 1.3))
        item_rows_meta.append({
          "pid": pid,
          "sku": sku,
          "price": price_adj,
          "main_img": f"https://cdn.example.com/items/{sku}/main.jpg",
          "item_status": item_status,
          "display_option": f"{opt1_name}: {v1} / {opt2_name}: {v2}",
          "is_default": is_first,
          "n_attrs": random.randint(1, 5),
        })
        is_first = False


def ensure_categories(cur, conn) -> dict:
  cur.execute("SELECT COUNT(*) FROM categories")
  count = cur.fetchone()[0]
  if count > 0:
    print(f"  → 기존 카테고리 {count}건 사용")
    cur.execute("SELECT id, name, depth FROM categories")
    rows = cur.fetchall()
    category_map = {}
    for (cid, cname, depth) in rows:
      if depth == 2:
        parent_weight = 1.0 / len(CATEGORY_TREE)
        for pname, pinfo in CATEGORY_TREE.items():
          if cname in pinfo["children"]:
            parent_weight = pinfo["weight"] / len(pinfo["children"])
            break
        category_map[cname] = {"id": cid, "depth": depth, "weight": parent_weight}
    return category_map

  print("  → 카테고리 신규 생성")
  category_map = {}

  cur.execute("""
              INSERT INTO categories (name, depth, parent_id, created_at, updated_at)
              VALUES ('전체', 0, NULL, NOW(), NOW()) RETURNING id
              """)
  root_id = cur.fetchone()[0]

  for parent_name, parent_info in CATEGORY_TREE.items():
    cur.execute("""
                INSERT INTO categories (name, depth, parent_id, created_at, updated_at)
                VALUES (%s, 1, %s, NOW(), NOW()) RETURNING id
                """, (parent_name, root_id))
    parent_id = cur.fetchone()[0]
    category_map[parent_name] = {"id": parent_id, "depth": 1, "weight": parent_info["weight"]}

    child_weight = parent_info["weight"] / len(parent_info["children"])
    for child_name in parent_info["children"]:
      cur.execute("""
                  INSERT INTO categories (name, depth, parent_id, created_at, updated_at)
                  VALUES (%s, 2, %s, NOW(), NOW()) RETURNING id
                  """, (child_name, parent_id))
      child_id = cur.fetchone()[0]
      category_map[child_name] = {"id": child_id, "depth": 2, "weight": child_weight}

  conn.commit()
  print(f"  → 카테고리 {len(category_map)}건 생성 완료")
  return category_map


def seed(target_products: int, chunk_size: int = 3_000):
  conn = psycopg2.connect(**DB_CONFIG)
  conn.autocommit = False
  cur = conn.cursor()

  print("=" * 60)
  print(f"[1/5] 카테고리 데이터 확인 및 초기화")
  print("=" * 60)
  category_map = ensure_categories(cur, conn)
  leaf_categories = [(cat_name, cat_info) for cat_name, cat_info in category_map.items()
                     if cat_info["depth"] == 2]

  cat_names   = [c[0] for c in leaf_categories]
  cat_weights = [c[1]["weight"] for c in leaf_categories]

  print(f"\n[2/5] 상품 {target_products:,}건 적재 시작")
  print(f"      chunk_size = {chunk_size:,}")

  total_products = 0
  total_items    = 0
  start_time = time.time()

  pbar = tqdm(total=target_products, unit="products")

  while total_products < target_products:
    current_chunk = min(chunk_size, target_products - total_products)

    # ── 상품(products) 생성 ──────────────────────────
    product_rows = []
    product_meta = []  # (cat_name, opt_type, status) 순서 보존용

    for _ in range(current_chunk):
      cat_name = random.choices(cat_names, weights=cat_weights, k=1)[0]
      cat_id   = category_map[cat_name]["id"]
      store_id = random.randint(1, 5456)
      pname    = random_product_name(cat_name)
      desc     = fake.text(max_nb_chars=300)

      opt_type = random.choices(
        ["NONE", "SINGLE", "DOUBLE"],
        weights=[0.10, 0.60, 0.30]
      )[0]

      status = random.choices(
        ["ON_SALE", "PENDING", "DISCONTINUED"],
        weights=[0.85, 0.10, 0.05]
      )[0]

      product_rows.append((cat_id, store_id, pname, desc, opt_type, status))
      product_meta.append((cat_name, opt_type, status))

    cur.executemany("""
                    INSERT INTO products (category_id, store_id, name, description, option_type, status,
                                          created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s,
                            NOW() - (random() * INTERVAL '365 days'),
                            NOW() - (random() * INTERVAL '30 days'))
                    """, product_rows)

    cur.execute("SELECT id FROM products ORDER BY id DESC LIMIT %s", (current_chunk,))
    inserted_pids = list(reversed([r[0] for r in cur.fetchall()]))

    # ── 상품 이미지 ─────────────────────────────────
    image_rows = []
    for pid in inserted_pids:
      n_images = random.choices([1, 2, 3, 4, 5], weights=[0.2, 0.35, 0.3, 0.1, 0.05])[0]
      for i in range(n_images):
        img_url  = f"https://cdn.example.com/products/{pid}/img_{i+1}.jpg"
        img_desc = random.choice(["대표 이미지", "상세 이미지", "착용 이미지", "측면 이미지", None])
        image_rows.append((pid, img_url, img_desc))

    if image_rows:
      cur.executemany("""
                      INSERT INTO product_images (product_id, image_url, description, created_at, updated_at)
                      VALUES (%s, %s, %s, NOW(), NOW())
                      """, image_rows)

    # ── 옵션값 + 아이템 메타 구성 ───────────────────
    option_value_rows = []
    item_rows_meta    = []

    for idx, pid in enumerate(inserted_pids):
      _, opt_type, p_status = product_meta[idx]

      if opt_type == "NONE":
        _add_item_meta(item_rows_meta, pid, opt_type, p_status)

      elif opt_type == "SINGLE":
        n_vals   = random.choices([2, 3, 4, 5, 6], weights=[0.2, 0.35, 0.3, 0.1, 0.05])[0]
        opt_name = random.choice(["사이즈", "색상", "용량", "패턴"])

        # ── BUG FIX: population을 먼저 변수로 분리 후 len()으로 상한 적용 ──
        population = (
          SIZES    if opt_name == "사이즈" else
          COLORS   if opt_name == "색상"  else
          VOLUME_ML if opt_name == "용량"  else
          PATTERNS
        )
        vals = random.sample(population, min(n_vals, len(population)))

        for disp_order, val in enumerate(vals):
          option_value_rows.append((pid, 1, opt_name, val, disp_order))

        _add_item_meta(item_rows_meta, pid, opt_type, p_status,
                       ov1_vals=vals, opt1_name=opt_name)

      elif opt_type == "DOUBLE":
        n_vals1  = random.choices([2, 3, 4], weights=[0.3, 0.4, 0.3])[0]
        n_vals2  = random.choices([2, 3, 4], weights=[0.3, 0.4, 0.3])[0]
        opt1_name = "사이즈"
        opt2_name = "색상"
        vals1 = random.sample(SIZES,  min(n_vals1, len(SIZES)))
        vals2 = random.sample(COLORS, min(n_vals2, len(COLORS)))

        for disp_order, val in enumerate(vals1):
          option_value_rows.append((pid, 1, opt1_name, val, disp_order))
        for disp_order, val in enumerate(vals2):
          option_value_rows.append((pid, 2, opt2_name, val, disp_order))

        _add_item_meta(item_rows_meta, pid, opt_type, p_status,
                       ov1_vals=vals1, ov2_vals=vals2,
                       opt1_name=opt1_name, opt2_name=opt2_name)

    if option_value_rows:
      cur.executemany("""
                      INSERT INTO product_option_values
                      (product_id, option_order, option_name, value, display_order, created_at, updated_at)
                      VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                      """, option_value_rows)

    # ── product_items INSERT ─────────────────────────
    item_insert_rows = [
      (r["pid"], r["sku"], r["price"], r["main_img"],
       r["item_status"], r["display_option"], r["is_default"])
      for r in item_rows_meta
    ]

    if item_insert_rows:
      cur.executemany("""
                      INSERT INTO product_items
                      (product_id, sku, price, main_image_url, status,
                       display_option_name, is_default, created_at, updated_at)
                      VALUES (%s, %s, %s, %s, %s, %s, %s,
                              NOW() - (random() * INTERVAL '365 days'),
                              NOW() - (random() * INTERVAL '30 days'))
                      """, item_insert_rows)

    cur.execute("SELECT id FROM product_items ORDER BY id DESC LIMIT %s",
                (len(item_insert_rows),))
    inserted_item_ids = list(reversed([r[0] for r in cur.fetchall()]))

    # ── 속성 + 재고 INSERT ───────────────────────────
    attr_rows  = []
    stock_rows = []

    for idx, item_id in enumerate(inserted_item_ids):
      meta    = item_rows_meta[idx]
      n_attrs = meta["n_attrs"]
      attrs   = pick_attributes_for_item(n_attrs)
      for disp_order, (attr_id, disp_name, _) in enumerate(attrs):
        attr_rows.append((item_id, attr_id, disp_name, disp_order))

      qty = random.randint(0, 500)
      if qty > 0:
        stock_status = random.choices(
          ["IN_STOCK", "OUT_OF_STOCK", "UNAVAILABLE"],
          weights=[0.78, 0.17, 0.05]
        )[0]
      else:
        stock_status = random.choices(
          ["OUT_OF_STOCK", "UNAVAILABLE"],
          weights=[0.85, 0.15]
        )[0]
      stock_rows.append((item_id, qty, stock_status))

    if attr_rows:
      cur.executemany("""
                      INSERT INTO product_attributes
                      (product_item_id, attribute_id, display_name, display_order, created_at, updated_at)
                      VALUES (%s, %s, %s, %s, NOW(), NOW())
                      """, attr_rows)

    if stock_rows:
      cur.executemany("""
                      INSERT INTO stocks (product_item_id, quantity, status, created_at, updated_at)
                      VALUES (%s, %s, %s, NOW(), NOW())
                      """, stock_rows)

    conn.commit()
    total_products += current_chunk
    total_items    += len(item_insert_rows)
    pbar.update(current_chunk)

  pbar.close()
  elapsed = time.time() - start_time
  print(f"\n[완료] product: {total_products:,}건 | product_item: {total_items:,}건")
  print(f"       소요시간: {elapsed:.1f}초 ({elapsed/60:.1f}분)")
  cur.close()
  conn.close()


if __name__ == "__main__":
  parser = argparse.ArgumentParser(description="이커머스 검색 도메인 테스트 데이터 적재 v2")
  parser.add_argument(
    "--batch", type=int, default=100_000,
    help="이번 실행에서 추가할 product 수 (기본: 100,000)"
  )
  parser.add_argument(
    "--chunk", type=int, default=3_000,
    help="한 트랜잭션 당 처리할 product 수 (기본: 3,000)"
  )
  args = parser.parse_args()
  seed(target_products=args.batch, chunk_size=args.chunk)