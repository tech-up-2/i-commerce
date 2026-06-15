# 검색 상품 데이터 적재 가이드

검색 도메인 부하테스트를 위한 더미 데이터 적재 스크립트입니다.  
상품(product) 기준 10만 ~ 수백만건의 데이터를 PostgreSQL에 적재할 수 있습니다.

---

## 파일 구성

```
k6/scripts
├── seed_product.py   # 데이터 적재 스크립트
└── README.md         # 이 문서
```

---

## 사전 준비

### 1. Python 버전 확인

```bash
python3 --version
# Python 3.10 이상 필요
```

### 2. 가상환경 생성 및 의존성 설치

```bash
# k6 디렉토리로 이동
cd k6/scripts

# 가상환경 생성 (최초 1회)
python3 -m venv venv

# 가상환경 활성화
source venv/bin/activate   # macOS / Linux
# venv\Scripts\activate    # Windows

# 의존성 설치 (최초 1회)
pip install faker psycopg2-binary tqdm
```

### 3. DB 접속 정보 설정

로컬 PostgreSQL(Docker)이 실행 중인 상태여야 합니다.  
환경변수로 접속 정보를 주입합니다.

```bash
export DB_HOST=localhost
export DB_PORT=<실제_port>
export DB_NAME=<실제_DB명>
export DB_USER=<실제_유저명>
export DB_PASSWORD=<실제_비밀번호>
```

---

## 데이터 적재 방법

### 기본 실행

```bash
# 100만건 적재
python seed_product.py --batch 1000000 --chunk 3000

# 10만건만 적재 (빠른 테스트용)
python seed_product.py --batch 100000 --chunk 3000
```

### 점진적 누적 적재 

스크립트는 **실행할 때마다 지정한 건수만큼 추가 적재**됩니다.  
아래 순서대로 실행하면 각 시점의 데이터 환경을 순차적으로 구성할 수 있습니다.

```bash
python seed_product.py --batch 100000   # 10만건
python seed_product.py --batch 400000   # 누적 50만건
python seed_product.py --batch 500000   # 누적 100만건
python seed_product.py --batch 1000000  # 누적 200만건
```

### 특정 시점 스냅샷 (롤백 필요 시)

특정 건수 시점으로 돌아가야 한다면, 적재 직후 Docker 이미지로 스냅샷을 찍어두세요.

```bash
# 컨테이너 ID 확인
docker ps

# 10만건 적재 직후 스냅샷
docker commit <container_id> ecommerce-db:100k

# 50만건 적재 직후 스냅샷
docker commit <container_id> ecommerce-db:500k

# 특정 시점으로 롤백 (새 컨테이너로 복원)
docker run -d -p 5433:5432 --name postgres_100k ecommerce-db:100k
```

---

## 옵션 설명

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `--batch` | 100,000 | 이번 실행에서 추가할 product 수 |
| `--chunk` | 3,000 | 한 트랜잭션에서 처리할 product 수. 메모리 부족 시 낮추세요 |

---

## 적재되는 테이블 및 예상 레코드 수

product 1건당 평균적으로 아래 레코드가 함께 생성됩니다.

| 테이블 | product 100만건 기준 |
|--------|----------------------|
| products | 1,000,000 |
| product_items | 약 3,200,000 (옵션 조합에 따라 다름) |
| product_images | 약 2,800,000 |
| product_option_values | 약 2,100,000 |
| product_attributes | 약 9,600,000 |
| stocks | product_items와 동일 |

> 로컬 SSD 환경 기준 100만건 적재에 약 20~40분 소요됩니다.

---

## 데이터 분포

실제 이커머스 서비스의 분포를 최대한 반영했습니다.

**카테고리 비중**

| 대분류 | 비중 |
|--------|------|
| 패션의류 | 28% |
| 전자기기 | 22% |
| 뷰티 | 13% |
| 생활가전 | 12% |
| 스포츠/레저 | 10% |
| 식품 | 8% |
| 도서/문구 | 4% |
| 완구/취미 | 3% |

**가격 분포**: 로그 정규분포 적용 (중위값 약 44,000원, 범위 1,000원 ~ 5,000,000원)

**상품 상태**: ON_SALE 85% / PENDING 10% / DISCONTINUED 5%

**옵션 타입**: SINGLE 60% / DOUBLE 30% / NONE 10%

**속성(attribute)**: 상품 아이템당 1~5개, 15가지 속성 풀에서 랜덤 선택

---

## 카테고리 재사용

스크립트는 실행 시 카테고리 테이블이 비어있는 경우에만 카테고리를 신규 생성합니다.  
**이미 카테고리가 있다면 기존 카테고리를 그대로 재사용하므로**, 반복 실행해도 중복 생성되지 않습니다.

---

## 적재 후 데이터 검증 쿼리

```sql
-- 상품 수 및 상태 분포
SELECT status, COUNT(*) FROM products GROUP BY status;

-- 카테고리별 상품 분포
SELECT c.name, COUNT(p.id) AS product_count
FROM products p
JOIN categories c ON p.category_id = c.id
GROUP BY c.name
ORDER BY product_count DESC;

-- 가격 분포 확인
SELECT
    ROUND(AVG(price))                                          AS avg_price,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY price)::INT   AS median_price,
    MIN(price),
    MAX(price)
FROM product_items;

-- 전체 테이블 레코드 수 한눈에 확인
SELECT
    (SELECT COUNT(*) FROM products)              AS products,
    (SELECT COUNT(*) FROM product_items)         AS product_items,
    (SELECT COUNT(*) FROM product_attributes)    AS product_attributes,
    (SELECT COUNT(*) FROM stocks)                AS stocks,
    (SELECT COUNT(*) FROM categories)            AS categories;
```

---

## 자주 발생하는 오류

**psycopg2 설치 실패**  
`psycopg2`가 아닌 반드시 `psycopg2-binary`를 설치하세요.
```bash
pip install psycopg2-binary
```

**M1/M2/M3 Mac에서 설치 오류**
```bash
arch -arm64 pip install psycopg2-binary
```

**DB 연결 실패**  
`docker-compose.yml`에 포트 바인딩이 설정되어 있는지 확인하세요.
```yaml
ports:
  - "5433:5432"
```

**메모리 부족**  
`--chunk` 값을 낮춰서 실행하세요.
```bash
python seed_product.py --batch 1000000 --chunk 1000
```

**적재 도중 중단**  
chunk 단위로 커밋하므로 중단 전 커밋된 데이터는 보존됩니다.  
그대로 재실행하면 이어서 누적 적재됩니다.