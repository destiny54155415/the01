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



-- =========================================================
-- PetShop 팀 프로젝트 DB 초기 스키마
-- Database: petshop
-- MySQL 8.0+
--
-- 사용 방법
-- 1) MySQL Workbench 또는 IntelliJ Database 콘솔에서 전체 실행
-- 2) 팀원은 동일한 SQL 파일로 테이블 구조를 맞춤
-- 3) 각자 로컬 MySQL에 petshop 데이터베이스를 만들고 실행
--
-- 주의
-- - 이 파일은 테이블 구조와 기본 카테고리 데이터를 생성합니다.
-- - 기존 테이블을 삭제하지 않습니다.
-- - 비밀번호는 평문으로 저장하지 말고 BCrypt 등으로 해시하여 저장하세요.
-- =========================================================

CREATE DATABASE IF NOT EXISTS petshop
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE petshop;

-- =========================================================
-- 1. 회원
-- =========================================================
CREATE TABLE IF NOT EXISTS members (
    member_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '로그인 아이디',
    password        VARCHAR(255) NOT NULL COMMENT '해시된 비밀번호',
    name            VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    zipcode         VARCHAR(10),
    address         VARCHAR(255),
    address_detail  VARCHAR(255),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_members_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 2. 상품 카테고리
-- =========================================================
CREATE TABLE IF NOT EXISTS categories (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(50) NOT NULL UNIQUE,
    display_order   INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO categories (category_name, display_order) VALUES
('사료', 1),
('간식', 2),
('장난감', 3),
('산책용품', 4),
('미용·목욕', 5),
('하우스·생활용품', 6),
('의류·패션', 7),
('위생·배변용품', 8);


-- =========================================================
-- 3. 상품
-- pet_type: DOG / CAT / COMMON
-- =========================================================
CREATE TABLE IF NOT EXISTS products (
    product_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT NOT NULL,
    product_name    VARCHAR(150) NOT NULL,
    description     TEXT,
    price           DECIMAL(12, 2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    pet_type        VARCHAR(20) NOT NULL DEFAULT 'COMMON',
    image_url       VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_stock CHECK (stock >= 0),
    CONSTRAINT chk_products_pet_type CHECK (pet_type IN ('DOG', 'CAT', 'COMMON')),
    INDEX idx_products_category (category_id),
    INDEX idx_products_name (product_name),
    INDEX idx_products_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 4. 장바구니 상품
-- 한 회원은 같은 상품을 장바구니에 한 행으로 보관
-- =========================================================
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uq_cart_member_product UNIQUE (member_id, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0),
    INDEX idx_cart_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 5. 주문
-- order_status: PENDING / PAID / SHIPPING / COMPLETED / CANCELLED
-- =========================================================
CREATE TABLE IF NOT EXISTS orders (
    order_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT NOT NULL,
    order_number     VARCHAR(40) NOT NULL UNIQUE,
    total_price      DECIMAL(12, 2) NOT NULL,
    order_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    recipient_name   VARCHAR(50) NOT NULL,
    recipient_phone  VARCHAR(20) NOT NULL,
    zipcode          VARCHAR(10),
    address          VARCHAR(255) NOT NULL,
    address_detail   VARCHAR(255),
    request_note     VARCHAR(500),
    ordered_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_member
        FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT chk_orders_total_price CHECK (total_price >= 0),
    CONSTRAINT chk_orders_status CHECK (
        order_status IN ('PENDING', 'PAID', 'SHIPPING', 'COMPLETED', 'CANCELLED')
    ),
    INDEX idx_orders_member_date (member_id, ordered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 6. 주문 상품
-- 주문 당시 상품명과 가격을 스냅샷으로 저장
-- =========================================================
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id         BIGINT NOT NULL,
    product_id       BIGINT,
    product_name     VARCHAR(150) NOT NULL,
    unit_price       DECIMAL(12, 2) NOT NULL,
    quantity         INT NOT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_order_items_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 7. 상품 리뷰
-- 회원은 상품별 리뷰를 하나만 작성하는 기본 설계
-- =========================================================
CREATE TABLE IF NOT EXISTS reviews (
    review_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    order_item_id   BIGINT,
    rating          TINYINT NOT NULL,
    content         TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
        ON DELETE SET NULL,
    CONSTRAINT uq_reviews_member_product UNIQUE (member_id, product_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_reviews_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 8. 찜 목록
-- =========================================================
CREATE TABLE IF NOT EXISTS wishlists (
    wishlist_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlists_member
        FOREIGN KEY (member_id) REFERENCES members(member_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_wishlists_member_product UNIQUE (member_id, product_id),
    INDEX idx_wishlists_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 생성 확인
-- =========================================================
SHOW TABLES;

