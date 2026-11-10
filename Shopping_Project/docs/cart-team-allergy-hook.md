# 장바구니 담당 연동 가이드 (상품 담당 제공)

상품 쪽은 `products.allergens` 와 `ProductAllergyService` 만 제공합니다.
Cart / Pet / Member 코드는 상품 담당이 수정하지 않습니다.

## 장바구니에서 호출 예

```java
@Autowired
ProductAllergyService productAllergyService;

// 회원의 반려동물 알러지 라벨 (펫 담당 데이터)
Set<String> allergies = Set.of("닭고기", "우유");

List<ProductEntity> productsInCart = ...; // 장바구니 상품들
List<ProductEntity> safe = productAllergyService.filterSafeProducts(productsInCart, allergies);
List<ProductEntity> unsafe = productAllergyService.filterUnsafeProducts(productsInCart, allergies);
```

알러지 라벨은 반려동물 등록 화면과 동일해야 합니다:

소고기, 돼지고기, 양고기, 오리고기, 닭고기, 연어, 계란, 우유, 치즈, 콩, 고구마, 감자
