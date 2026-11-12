# 냥마트 상품 모듈

4명 팀 프로젝트에서 **2번 상품 관리**만 구현한 화면입니다. 참고한 흐름은 [캣팡](https://www.catpang.com)의 카테고리 목록, 상품 검색, 정렬입니다.

담당 범위는 상품, 카테고리, 검색입니다. 회원가입, 장바구니, 주문, 리뷰 작성은 없습니다. 상품에 보이는 평균 평점은 저장된 값이고, 후기 담당이 `ProductService.updateRating`으로 갱신합니다.

## 실행

```bash
mvn spring-boot:run
```

브라우저에서 http://localhost:8080/products 를 엽니다. 상품 관리는 http://localhost:8080/admin/products 입니다.

다른 담당이 상품을 조회할 때는 아래 주소를 사용합니다.

- `GET /api/categories`
- `GET /api/products?q=모래&categoryId=1&sort=PRICE_ASC&page=0`
- `GET /api/products/{id}`

`sort` 값은 `NEWEST`, `NAME`, `PRICE_ASC`, `PRICE_DESC`, `POPULAR`, `REVIEWS` 입니다.
