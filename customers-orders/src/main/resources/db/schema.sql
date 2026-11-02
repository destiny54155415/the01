-- =========================================
-- 1. 고객 (customers)
-- =========================================
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '고객명',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일',
    phone VARCHAR(30) COMMENT '연락처',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT = '고객';

-- =========================================
-- 2. 주문 (orders)
-- =========================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL COMMENT '주문한 고객',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '주문번호',
    total_price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '주문 금액',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '주문 상태',
    ordered_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '주문 일시',

    --FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) COMMENT = '주문';
