package com.team.catalog.config;

import com.team.catalog.domain.Category;
import com.team.catalog.domain.Product;
import com.team.catalog.domain.ProductStatus;
import com.team.catalog.repository.CategoryRepository;
import com.team.catalog.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class CatalogDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogDataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        Category food = root("사료", 1);
        Category snack = root("간식", 2);
        Category litter = root("모래/화장실", 3);
        Category tower = root("스크래쳐/캣타워", 4);
        Category bath = root("목욕/미용", 5);
        Category toy = root("장난감", 6);

        Category dry = child(food, "건식사료", 1);
        Category wet = child(food, "습식/캔", 2);
        Category churu = child(snack, "츄르/파우치", 1);
        Category freeze = child(snack, "동결건조", 2);
        Category tofu = child(litter, "두부모래", 1);
        Category clay = child(litter, "벤토나이트", 2);
        Category box = child(litter, "화장실", 3);
        Category scratch = child(tower, "스크래쳐", 1);
        Category catTower = child(tower, "캣타워", 2);
        Category brush = child(bath, "브러쉬", 1);
        Category shampoo = child(bath, "샴푸", 2);
        Category wand = child(toy, "낚시대", 1);
        Category doll = child(toy, "공/인형", 2);

        product(dry, "실내 고양이 건식사료 2kg", "냥테이블", 32000, 28900, "헤어볼 케어가 들어간 실내묘 사료", 40, 860, 128, 4.8);
        product(dry, "어덜트 라이트 사료 1.4kg", "힐테이블", 26000, null, "체중 관리용 성묘 사료", 22, 240, 46, 4.6);
        product(wet, "참치 주식캔 85g 12개", "오션냥", 18000, 15900, "주식으로 급여하는 참치 캔", 55, 510, 210, 4.9);
        product(churu, "닭가슴살 짜먹는 간식 4개입", "냥츄", 2200, null, "기호성 높은 주식 파우치", 120, 1500, 640, 4.7);
        product(freeze, "동결건조 치킨 큐브 80g", "캣큐브", 14500, null, "트릿과 토핑으로 쓰는 치킨", 33, 300, 88, 4.5);
        product(tofu, "먼지적은 두부모래 2.6kg", "두부냥", 6900, null, "응고력이 빠른 오리지널 두부모래", 80, 2200, 980, 4.8);
        product(clay, "무향 벤토나이트 6kg", "모래결", 8900, 7900, "먼지 억제 벤토나이트", 48, 640, 190, 4.4);
        product(box, "대형 후드 화장실", "냥집", 29000, null, "큰 고양이도 여유 있는 후드형", 15, 180, 24, 4.3);
        product(scratch, "소파 스크래쳐", "할퀸", 15900, null, "교체 골판지가 들어 있는 소파형", 27, 410, 73, 4.6);
        product(catTower, "5단 캣타워", "높은집", 89000, 79000, "해먹과 스크래쳐가 있는 타워", 8, 260, 31, 4.7);
        product(brush, "죽은털 제거 브러쉬", "빗질냥", 12000, null, "환절기 그루밍용 슬리커", 36, 390, 54, 4.2);
        product(shampoo, "저자극 샴푸 300ml", "버블냥", 15000, null, "피부 자극이 적은 무향 샴푸", 19, 90, 12, 4.1);
        product(wand, "애벌레 줄 낚시대", "흔들냥", 5500, null, "긴 줄로 사냥 본능을 자극", 70, 720, 140, 4.9);
        product(doll, "캣닢 생쥐 인형", "찍찍", 3900, null, "캣닢이 들어간 작은 인형", 90, 480, 66, 4.4);

        Product hidden = product(dry, "비공개 샘플 사료", "냥테이블", 10000, null, "목록에 나오면 안 되는 상품", 1, 0, 0, 0);
        hidden.setStatus(ProductStatus.HIDDEN);
        Product soldOut = product(wand, "깃털 낚시대", "흔들냥", 4500, null, "재고가 없는 낚시대", 0, 80, 15, 4.0);
        soldOut.setStatus(ProductStatus.SOLD_OUT);
        soldOut.setStock(0);
    }

    private Category root(String name, int sortOrder) {
        return categoryRepository.save(new Category(name, null, sortOrder));
    }

    private Category child(Category parent, String name, int sortOrder) {
        return categoryRepository.save(new Category(name, parent, sortOrder));
    }

    private Product product(Category category, String name, String brand, int price, Integer salePrice,
                            String summary, int stock, int views, int reviews, double rating) {
        Product product = new Product(category, name, brand, price, salePrice, summary, stock);
        product.setDescription(summary + "\n구성과 급여량은 상품 상세에서 확인합니다.");
        for (int i = 0; i < views; i++) {
            product.increaseViewCount();
        }
        product.replaceRating(reviews, rating);
        return productRepository.save(product);
    }
}
