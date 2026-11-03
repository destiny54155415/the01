🛍️ Shopping_Project
Java Spring Boot 기반 펫 쇼핑몰 프로젝트
👥 팀원별 역할 분담

본 프로젝트는 총 4명의 팀원으로 구성되어 있으며, 담당 기능에 따라 프론트엔드와 백엔드 개발을 분담하여 진행합니다.

1️⃣ 회원 담당 (Member) 민하연

담당 기능: 회원가입 · 로그인 · 마이페이지

🖥️ Frontend

회원가입 페이지
로그인 페이지
마이페이지
회원정보 수정 페이지

⚙️ Backend

Member Entity 구현
회원가입 및 로그인 처리
비밀번호 암호화
세션 로그인 및 로그아웃
회원정보 조회 및 수정

2️⃣ 상품 담당 (Product) 소강민

담당 기능: 상품 · 카테고리 · 검색

🖥️ Frontend

상품 목록 페이지
상품 상세 페이지
카테고리별 상품 페이지
상품 검색 화면

⚙️ Backend

Product, Category Entity 구현
상품 목록 조회
카테고리별 상품 조회
상품 상세 조회
상품 검색
관리자 상품 등록 / 수정 / 삭제

3️⃣ 장바구니 담당 (Cart & Wishlist) 노경빈

담당 기능: 장바구니 · 찜하기

🖥️ Frontend

장바구니 페이지
장바구니 상품 수량 변경
장바구니 상품 삭제
찜 목록 페이지

⚙️ Backend

CartItem, Wishlist Entity 구현
장바구니 담기
장바구니 목록 조회
장바구니 상품 수량 변경 및 삭제
장바구니 총금액 계산
찜하기 / 찜 취소

4️⃣ 주문 및 리뷰 담당 (Order & Review) 김승연

담당 기능: 주문 · 결제(모의) · 리뷰

🖥️ Frontend

주문서 페이지
주문 완료 페이지
주문 내역 페이지
주문 상세 페이지
리뷰 작성 및 목록 페이지

⚙️ Backend

Order, OrderItem, Review Entity 구현
주문 생성
주문 내역 조회
주문 상태 변경
주문 상세 조회
리뷰 작성 / 조회 / 수정 / 삭제
상품별 평균 평점 계산

📋 역할 분담 요약
담당자	담당 영역	주요 기능

1번	회원 관리	회원가입, 로그인, 마이페이지

2번	상품 관리	상품, 카테고리, 검색

3번	장바구니 관리	장바구니, 찜하기

4번	주문 및 리뷰 관리	주문, 모의 결제, 리뷰

---

## 코딩 시 주의사항

### 폴더 및 파일 규칙
머지·코드 리뷰가 쉽도록 아래 구조를 유지합니다.

**Java**
- `controller` / `service` / `repository` / `entity` / `dto` / `config`
- 기능별 하위 패키지 예 (상품 담당):
  - `controller/productcontroller`
  - `service/productservice`
  - `repository/productrepository`
  - `entity/productentity`
  - `dto/productdto`
  - (회원 예: `controller/membercontroller`, `service/memberservice` …)

**HTML (templates)**
- 공통 조각: `templates/fragments/` → `header.html`, `category-nav.html`, `footer.html`
- 화면: `templates/member|product|cart|order|review/`

**정적 자원**
- `static/css/common.css` (공통)
- 기능 CSS 예: `static/css/product.css`
- `static/js/`, `static/images/`

**DB**
- 스키마 기준 파일: `db/petshop_db.sql` (프로젝트 루트)

### GitHub 협업 방식
- `main`: 실행 가능한 공통 코드 + 리뷰 완료 기능만
- 기능 브랜치: `feature/member`, `feature/product`, `feature/cart`, `feature/order-review`
- 흐름: 작업 → commit → push → PR → 동작 확인 → (팀원 리뷰 권장) → `main` 머지
- 공통 HTML/CSS, DB 스키마, `application.properties` 변경은 **수정 전에 팀 공유**

상품 담당은 반드시 `feature/product` 브랜치에서 작업합니다. **main에 직접 커밋하지 마세요.**




