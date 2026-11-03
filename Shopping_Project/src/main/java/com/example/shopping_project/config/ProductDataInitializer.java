package com.example.shopping_project.config;

import com.example.shopping_project.entity.productentity.Category;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.Product;
import com.example.shopping_project.repository.productrepository.CategoryRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 펫샵에서 잘 팔리는 SKU 위주로 카탈로그를 맞춘다.
 * 동일 상품명이 있으면 가격·재고·판매량을 갱신하고, 옛 샘플명은 비활성화한다.
 */
@Component
@Profile("!test")
public class ProductDataInitializer implements CommandLineRunner {

    private static final Set<String> LEGACY_SAMPLE_NAMES = Set.of(
            "실내묘 건식사료 2kg",
            "참치 주식캔 12개입",
            "짜먹는 간식 참치맛 4개입",
            "동결건조 치킨 트릿 80g",
            "애벌레 줄 낚시대",
            "캣닢 생쥐 인형",
            "리드줄 기본형",
            "저자극 샴푸 300ml",
            "숨숨집 하우스",
            "후드 티셔츠",
            "먼지적은 두부모래 2.6kg",
            "벤토나이트 모래 6kg"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductDataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCategoriesIfEmpty();
        syncBestsellers();
        deactivateLegacySamples();
    }

    private void seedCategoriesIfEmpty() {
        if (categoryRepository.count() > 0) {
            return;
        }
        List.of(
                new Category("사료", 1),
                new Category("간식", 2),
                new Category("장난감", 3),
                new Category("산책용품", 4),
                new Category("미용·목욕", 5),
                new Category("하우스·생활용품", 6),
                new Category("의류·패션", 7),
                new Category("위생·배변용품", 8)
        ).forEach(categoryRepository::save);
    }

    private void syncBestsellers() {
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscCategoryIdAsc();
        if (categories.size() < 8) {
            return;
        }
        Category food = categories.get(0);
        Category snack = categories.get(1);
        Category toy = categories.get(2);
        Category walk = categories.get(3);
        Category bath = categories.get(4);
        Category house = categories.get(5);
        Category fashion = categories.get(6);
        Category hygiene = categories.get(7);

        // salesCount = 상대적 인기(높을수록 메인 추천 상단)
        upsert(food, "실내묘 헤어볼 케어 사료 2kg",
                "국내 펫샵 스테디셀러. 헤어볼·실내묘 전용 건식 사료입니다.",
                "32900", 85, 1820, PetType.CAT);
        upsert(food, "소형견 전연령 건식사료 2kg",
                "소형견 보호자 구매가 많은 기본 사료입니다.",
                "29900", 70, 1410, PetType.DOG);
        upsert(food, "고양이 주식 파우치 12개입",
                "습식 주식으로 자주 재구매되는 파우치 세트입니다.",
                "18900", 95, 1560, PetType.CAT);

        upsert(snack, "짜먹는 츄르 참치 20개입",
                "고양이 간식 부동의 1위. 기호성 좋은 참치 츄르입니다.",
                "12900", 200, 4200, PetType.CAT);
        upsert(snack, "짜먹는 츄르 연어 20개입",
                "참치와 함께 가장 많이 팔리는 연어맛 츄르입니다.",
                "12900", 180, 3650, PetType.CAT);
        upsert(snack, "강아지 져키 간식 200g",
                "산책·훈련 보상용으로 재구매율이 높은 져키입니다.",
                "9900", 110, 980, PetType.DOG);
        upsert(snack, "동결건조 트릿 60g",
                "토핑·훈련용으로 인기인 동결건조 간식입니다.",
                "15900", 75, 1120, PetType.COMMON);

        upsert(toy, "고양이 낚싯대 2종 세트",
                "사냥 본능 자극용으로 꾸준히 팔리는 기본 장난감입니다.",
                "8900", 120, 1340, PetType.CAT);
        upsert(toy, "캣닢 쿠션 토이",
                "캣닢 장난감 중 재고 회전이 빠른 인기 상품입니다.",
                "5900", 140, 1210, PetType.CAT);
        upsert(toy, "강아지 공 장난감 3개입",
                "실내 놀이용으로 많이 찾는 저가 묶음 상품입니다.",
                "7900", 90, 760, PetType.DOG);

        upsert(walk, "쿠션 하네스 M",
                "산책 필수템. 목줄 대신 많이 선택되는 하네스입니다.",
                "24900", 55, 890, PetType.DOG);
        upsert(walk, "자동 리드줄 5m",
                "중형견 보호자에게 판매량이 높은 자동 리드줄입니다.",
                "21900", 48, 720, PetType.DOG);

        upsert(bath, "저자극 펫샴푸 500ml",
                "목욕 시즌·정기 구매가 많은 저자극 샴푸입니다.",
                "16900", 60, 640, PetType.COMMON);
        upsert(bath, "털제거 브러시",
                "털갈이 시즌마다 판매가 크게 오르는 브러시입니다.",
                "12900", 80, 880, PetType.COMMON);

        upsert(house, "고양이 스크래쳐 하우스",
                "스크래쳐+숨숨집 겸용으로 객단가가 높은 인기 상품입니다.",
                "39900", 35, 970, PetType.CAT);
        upsert(house, "극세사 펫쿠션 L",
                "강아지·고양이 공용으로 회전율이 좋은 방석입니다.",
                "24900", 50, 830, PetType.COMMON);

        upsert(fashion, "방수 올인원 레인코트",
                "비 오는 날 수요가 몰리는 강아지 레인코트입니다.",
                "27900", 28, 310, PetType.DOG);

        upsert(hygiene, "먼지 적은 두부모래 7L",
                "고양이 모래 카테고리 최다 판매. 응고력·저분진 타입입니다.",
                "11900", 220, 5100, PetType.CAT);
        upsert(hygiene, "벤토나이트 모래 10kg",
                "대용량으로 가성비 구매가 많은 벤토나이트 모래입니다.",
                "14900", 160, 3900, PetType.CAT);
        upsert(hygiene, "배변패드 50매",
                "소형견 가정에서 정기 구매하는 기본 배변패드입니다.",
                "15900", 100, 1180, PetType.DOG);
        upsert(hygiene, "모래삽 + 보관통 세트",
                "모래 구매 시 함께 담기는 교차판매 인기 세트입니다.",
                "7900", 95, 1450, PetType.CAT);
    }

    private void upsert(Category category, String name, String description, String price,
                        int stock, int salesCount, PetType petType) {
        productRepository.findByProductName(name).ifPresentOrElse(existing -> {
            existing.setCategory(category);
            existing.setDescription(description);
            existing.setPrice(new BigDecimal(price));
            existing.setStock(stock);
            existing.setSalesCount(salesCount);
            existing.setPetType(petType);
            existing.setIsActive(true);
        }, () -> productRepository.save(new Product(
                category, name, description, new BigDecimal(price), stock, salesCount, petType, null)));
    }

    private void deactivateLegacySamples() {
        for (String name : LEGACY_SAMPLE_NAMES) {
            productRepository.findByProductName(name).ifPresent(product -> product.setIsActive(false));
        }
    }
}
