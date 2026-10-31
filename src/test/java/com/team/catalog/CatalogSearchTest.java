package com.team.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team.catalog.domain.Category;
import com.team.catalog.domain.Product;
import com.team.catalog.domain.ProductSort;
import com.team.catalog.domain.ProductStatus;
import com.team.catalog.repository.CategoryRepository;
import com.team.catalog.repository.ProductRepository;
import com.team.catalog.service.CategoryService;
import com.team.catalog.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogSearchTest {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    MockMvc mockMvc;

    Category dry;
    Category tofu;

    @BeforeEach
    void setUp() {
        Category food = categoryRepository.save(new Category("사료", null, 1));
        Category litter = categoryRepository.save(new Category("모래/화장실", null, 2));
        dry = categoryRepository.save(new Category("건식사료", food, 1));
        tofu = categoryRepository.save(new Category("두부모래", litter, 1));
        save(dry, "실내 건식사료", "냥테이블", 30000, 25000, "실내묘 사료", ProductStatus.ON_SALE, 10);
        save(tofu, "먼지적은 두부모래", "두부냥", 7000, null, "응고가 빠른 모래", ProductStatus.ON_SALE, 50);
        Product hidden = save(dry, "숨긴 사료", "냥테이블", 1000, null, "비공개", ProductStatus.HIDDEN, 1);
        hidden.setStatus(ProductStatus.HIDDEN);
    }

    @Test
    void searchesByNameBrandAndSummary() {
        assertThat(productService.search("두부", null, ProductSort.NEWEST, 0, false).getContent())
                .extracting(Product::getName)
                .containsExactly("먼지적은 두부모래");
        assertThat(productService.search("냥테이블", null, ProductSort.NAME, 0, false).getContent())
                .extracting(Product::getName)
                .containsExactly("실내 건식사료");
    }

    @Test
    void parentCategoryIncludesChildrenAndSkipsHidden() {
        Category food = dry.getParent();
        assertThat(productService.search(null, food.getId(), ProductSort.NEWEST, 0, false).getContent())
                .extracting(Product::getName)
                .containsExactly("실내 건식사료");
    }

    @Test
    void sortsBySellingPrice() {
        assertThat(productService.search(null, null, ProductSort.PRICE_ASC, 0, false).getContent())
                .extracting(Product::getName)
                .containsExactly("먼지적은 두부모래", "실내 건식사료");
    }

    @Test
    void blocksDeletingCategoryThatHasProducts() {
        assertThatThrownBy(() -> categoryService.delete(dry.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품");
    }

    @Test
    void createsProductInChildCategory() throws Exception {
        mockMvc.perform(post("/admin/products")
                        .param("categoryId", String.valueOf(tofu.getId()))
                        .param("name", "벤토나이트 모래")
                        .param("brand", "모래결")
                        .param("price", "9000")
                        .param("salePrice", "")
                        .param("stock", "3")
                        .param("status", "ON_SALE"))
                .andExpect(status().is3xxRedirection());
        assertThat(productRepository.findAll())
                .extracting(Product::getName)
                .contains("벤토나이트 모래");
    }

    @Test
    void searchPageShowsMatchedProduct() throws Exception {
        mockMvc.perform(get("/products").param("q", "건식"))
                .andExpect(status().isOk());
    }

    @Test
    void categoryPageRendersChildProducts() throws Exception {
        mockMvc.perform(get("/products").param("categoryId", String.valueOf(dry.getParent().getId())))
                .andExpect(status().isOk());
    }

    private Product save(Category category, String name, String brand, int price, Integer salePrice,
                         String summary, ProductStatus status, int stock) {
        Product product = new Product(category, name, brand, price, salePrice, summary, stock);
        product.setStatus(status);
        return productRepository.save(product);
    }
}
