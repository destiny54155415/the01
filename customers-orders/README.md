# 고객 · 주문 CRUD

`customers` 1명과 `orders` 여러 건을 웹에서 등록, 조회, 수정, 삭제합니다.

- Spring Boot, Spring Data JPA
- 조회 메서드: `findByEmail`, `findByOrderNumber`, `findByCustomerIdOrderByOrderedAtDesc`
- JPQL: 고객별 주문 통합 조회 `findOrdersOfCustomer`, 전체 주문 `findAllWithCustomer`
- 화면: Thymeleaf

과제 SQL에는 외래키가 주석으로 남아 있습니다. 데이터베이스 제약 대신, 고객을 삭제할 때 해당 `customer_id`의 주문을 먼저 삭제합니다.

## 실행

Java 17 이상이 필요합니다. IntelliJ에서 `customers-orders` 폴더를 열고 `CustomerOrderApplication`을 실행합니다.

기본 설정은 H2 메모리 데이터베이스입니다. 실행 후 http://localhost:8080 을 엽니다. 샘플로 홍길동과 주문 2건이 들어 있습니다.

MySQL을 쓸 때는 `src/main/resources/db/schema.sql`을 실행한 뒤 프로필을 바꿉니다.

```text
--spring.profiles.active=mysql
```

접속 정보는 `application-mysql.properties`에 있습니다. 기본 계정은 `root` / `1234`, 데이터베이스 이름은 `customer_order`입니다.
