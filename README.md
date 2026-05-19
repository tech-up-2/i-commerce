# 📦 I-commerce
> **쇼핑부터 실시간 소통까지, 쿠팡 클론 백엔드 시스템**

회원가입, 상품 검색부터 안전한 결제, 실시간 채팅 및 리뷰 작성까지 이커머스 쇼핑의 모든 과정을 하나의 서비스로 완결하는 O5O 팀의 백엔드 프로젝트입니다.

---

## 👥 프로젝트 및 O5O 팀원 소개

| 이름 | 담당 도메인 (Core Domain) | 주요 구현 내용 |
| :---: | :--- | :--- |
| **신지훈** | **주문/결제** | 주문 생성 및 관리, 가상 결제 연동 |
| **문시원** | **상품** | 상품 관리 및 검색/필터링, 재고 관리 |
| **김준영** | **회원** | 회원가입/로그인, 회원/판매자 및 상점 관리 |
| **고정국** | **채팅** | 실시간 1:1 채팅, 채팅 내역 조회 및 상태 관리 |
| **노여진** | **리뷰** | 리뷰 작성, 다중 사진 업로드, 판매자 답글, 권한 처리 |

<br>

## 🛠 기술 스택 (Tech Stack)

| 분류 | 기술 (Tech Stack) |
| :--- | :--- |
| **Language** | Java 25 |
| **Framework** | Spring Boot 4.0.5, Spring Security |
| **Database** | PostgreSQL |
| **Realtime** | WebSocket (STOMP) |
| **Storage** | AWS S3 (이미지 및 미디어 파일 스토리지) |
| **Infra & DevOps** | Docker, GitHub Actions, AWS |
| **Authentication** | JWT (JSON Web Token) |

<br>

## 💻 설치 및 실행 방법 (Installation & Setup)
<br>

## 🚀 주요 기능 및 사용법 (Usage)

| 도메인 | 핵심 기능 설명 | 테스트/확인 방법 (API) |
| :--- | :--- | :--- |
| **회원 (Member)** | 회원가입, 로그인 및 권한/정보 관리 | `POST /api/v1/auth/signup` |
| **상품 (Product)** | 카테고리 분류 및 조건별 상품 검색/필터링 | `GET /api/v1/products` |
| **주문 (Order)** | 주문 생성 및 안정적인 재고 차감 처리 | `POST /api/v1/orders` |
| **결제 (Payment)** | 가상 결제 모듈 연동 및 결제 상태 추적 | `POST /api/v1/payments/charge` |
| **채팅 (Chat)** | 고객-판매자 간 실시간 1:1 소통 및 내역 조회 | `POST /api/v1/chat` |
| **리뷰 (Review)** | S3 사진 업로드 및 판매자 권한별 답글 작성 | `POST /api/v1/reviews` |
<br>

## 🤝 기여 방법 및 규칙 (Contributing)
본 프로젝트의 코드 품질 향상과 원활한 협업을 위해 O5O 팀은 아래 규칙을 철저히 준수합니다.

### 1. 브랜치 전략 (Git Flow)
* `main` : 상용 배포 브랜치
* `develop` : 개발 통합 브랜치 (모든 PR의 Base 브랜치)
* `feature/#이슈번호-기능명` : 단위 기능 개발 브랜치

### 2. 커밋 컨벤션 (Commit Convention)
* `feat:` 새로운 기능 추가
* `fix:` 버그 수정
* `docs:` 문서 수정 (README 등)
* `refactor:` 코드 리팩토링 (기능 변화 없음)

<br>

## 🔗 공유 문서 및 링크

| 문서 분류 | 설명 및 바로가기 |
| :--- | :--- |
| **프로젝트 기획서** | [I-commerce 기획안 및 요구사항 명세서](#) |
| **데이터 아키텍처** | [Draw.io / ERDCloud 스키마 구조도](#) |
| **API 명세서** | [Swagger API 정적 문서 주소](#) |