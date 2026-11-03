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
