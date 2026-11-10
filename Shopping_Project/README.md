# Shopping_Project

Java Spring Boot 기반 펫 쇼핑몰 프로젝트

## 팀원별 역할 분담

본 프로젝트는 총 4명의 팀원으로 구성되어 있으며, 담당 기능에 따라 프론트엔드와 백엔드 개발을 분담하여 진행합니다.

### 1. 회원 담당 (Member) — 민하연

- 회원가입 · 로그인 · 마이페이지
- Member Entity, 비밀번호 암호화, 세션 로그인/로그아웃, 회원정보 조회·수정

### 2. 상품 담당 (Product) — 소강민

- 상품 · 카테고리 · 검색
- Product/Category Entity, 목록·상세·검색, 관리자 상품 등록/수정/삭제
- 홈 필터(카테고리·동물·금액), 상세 갤러리 이미지

### 3. 장바구니 담당 (Cart & Wishlist) — 노경빈

- 장바구니 · 찜하기
- CartItem/Wishlist Entity, 담기·수량변경·삭제·총금액, 찜하기/취소

### 4. 주문 및 리뷰 담당 (Order & Review) — 김승연

- 주문 · 결제(모의) · 리뷰
- Order/OrderItem/Review Entity, 주문 생성·내역·상태, 리뷰 CRUD, 평균 평점

## 실행

- Java 25 / Spring Boot 4.1.2
- MySQL `petshop` 또는 H2
- `./gradlew bootRun` → http://localhost:8080/
