package com.example.shopping_project.config.product;

import com.example.shopping_project.entity.product.Category;
import com.example.shopping_project.entity.product.PetType;
import com.example.shopping_project.entity.product.Product;
import com.example.shopping_project.repository.product.CategoryRepository;
import com.example.shopping_project.repository.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class ProductDataInitializer implements CommandLineRunner {

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
        seedProductsIfEmpty();
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

    private void seedProductsIfEmpty() {
        if (productRepository.count() > 0) {
            return;
        }
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAscCategoryIdAsc();
        Category food = categories.get(0);
        Category snack = categories.get(1);
        Category toy = categories.get(2);
        Category walk = categories.get(3);
        Category bath = categories.get(4);
        Category house = categories.get(5);
        Category fashion = categories.get(6);
        Category hygiene = categories.get(7);

        productRepository.save(new Product(food, "실내묘 건식사료 2kg", "헤어볼 케어가 들어간 고양이 사료입니다.",
                new BigDecimal("28900"), 40, PetType.CAT, null));
        productRepository.save(new Product(food, "참치 주식캔 12개입", "주식으로 급여하는 고양이 캔입니다.",
                new BigDecimal("15900"), 55, PetType.CAT, null));
        productRepository.save(new Product(snack, "짜먹는 간식 참치맛 4개입", "기호성 좋은 고양이 간식입니다.",
                new BigDecimal("2200"), 120, PetType.CAT, null));
        productRepository.save(new Product(snack, "동결건조 치킨 트릿 80g", "토핑과 보상용으로 사용합니다.",
                new BigDecimal("14500"), 33, PetType.COMMON, null));
        productRepository.save(new Product(toy, "애벌레 줄 낚시대", "사냥 본능을 자극하는 낚시대입니다.",
                new BigDecimal("5500"), 70, PetType.CAT, null));
        productRepository.save(new Product(toy, "캣닢 생쥐 인형", "캣닢이 들어간 작은 인형입니다.",
                new BigDecimal("3900"), 90, PetType.CAT, null));
        productRepository.save(new Product(walk, "리드줄 기본형", "산책용 기본 리드줄입니다.",
                new BigDecimal("9800"), 25, PetType.DOG, null));
        productRepository.save(new Product(bath, "저자극 샴푸 300ml", "피부 자극이 적은 무향 샴푸입니다.",
                new BigDecimal("15000"), 19, PetType.COMMON, null));
        productRepository.save(new Product(house, "숨숨집 하우스", "아늑한 고양이 휴식 공간입니다.",
                new BigDecimal("32000"), 12, PetType.CAT, null));
        productRepository.save(new Product(fashion, "후드 티셔츠", "가볍게 입을 수 있는 후드입니다.",
                new BigDecimal("18000"), 15, PetType.DOG, null));
        productRepository.save(new Product(hygiene, "먼지적은 두부모래 2.6kg", "응고력이 빠른 두부모래입니다.",
                new BigDecimal("6900"), 80, PetType.CAT, null));
        productRepository.save(new Product(hygiene, "벤토나이트 모래 6kg", "먼지 억제형 벤토나이트입니다.",
                new BigDecimal("7900"), 48, PetType.CAT, null));
    }
}
