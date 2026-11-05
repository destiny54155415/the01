package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.productrepository.CategoryRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import com.example.shopping_project.service.productservice.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductSearchTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductService productService;

    CategoryEntity food;
    CategoryEntity snack;

    @BeforeEach
    void setUp() {
        food = categoryRepository.save(new CategoryEntity("사료", 1));
        snack = categoryRepository.save(new CategoryEntity("간식", 2));
        productRepository.save(new ProductEntity(food, "실내묘 건식사료", "고양이 사료",
                new BigDecimal("20000"), 10, PetType.CAT, null));
        productRepository.save(new ProductEntity(snack, "참치 츄르", "고양이 간식",
                new BigDecimal("3000"), 50, PetType.CAT, null));
        ProductEntity hidden = new ProductEntity(food, "숨긴 사료", "비공개",
                new BigDecimal("1000"), 1, PetType.DOG, null);
        hidden.setIsActive(false);
        productRepository.save(hidden);
    }

    @Test
    void searchesByKeywordAndCategory() {
        assertThat(productService.searchActive("츄르", null, null, null, null, 0).getContent())
                .extracting(ProductEntity::getProductName)
                .containsExactly("참치 츄르");
        assertThat(productService.searchActive(null, food.getCategoryId(), PetType.CAT, null, null, 0).getContent())
                .extracting(ProductEntity::getProductName)
                .containsExactly("실내묘 건식사료");
        assertThat(productService.searchActive(null, null, null, null, null, 0).getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchesByPriceRange() {
        assertThat(productService.searchActive(null, null, null,
                new BigDecimal("10000"), new BigDecimal("30000"), 0).getContent())
                .extracting(ProductEntity::getProductName)
                .containsExactly("실내묘 건식사료");
        assertThat(productService.searchActive(null, null, null,
                new BigDecimal("0"), new BigDecimal("10000"), 0).getContent())
                .extracting(ProductEntity::getProductName)
                .containsExactly("참치 츄르");
    }

    @Test
    void productListPageWorks() throws Exception {
        mockMvc.perform(get("/products").param("keyword", "건식"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products").param("categoryId", String.valueOf(snack.getCategoryId())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products").param("priceRange", "FROM_10000_TO_30000"))
                .andExpect(status().isOk());
    }

    @Test
    void homeShowsProductListDirectly() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/").param("priceRange", "UNDER_10000"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().string(org.hamcrest.Matchers.containsString("참치 츄르")));
        mockMvc.perform(get("/").param("priceRange", "FROM_10000_TO_30000"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().string(org.hamcrest.Matchers.containsString("실내묘 건식사료")));
    }

    @Test
    void bestsellersOrderedBySalesCount() {
        ProductEntity low = productRepository.findAll().stream()
                .filter(p -> "참치 츄르".equals(p.getProductName()))
                .findFirst()
                .orElseThrow();
        low.setSalesCount(10);
        ProductEntity high = productRepository.findAll().stream()
                .filter(p -> "실내묘 건식사료".equals(p.getProductName()))
                .findFirst()
                .orElseThrow();
        high.setSalesCount(999);
        productRepository.flush();

        assertThat(productService.findBestsellers(2))
                .extracting(ProductEntity::getProductName)
                .containsExactly("실내묘 건식사료", "참치 츄르");
    }

    @Test
    void adminCanCreateProductWithSessionRole() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginRole", "ADMIN");

        mockMvc.perform(post("/admin/products")
                        .session(session)
                        .param("categoryId", String.valueOf(food.getCategoryId()))
                        .param("productName", "캣타워")
                        .param("description", "고양이 타워")
                        .param("price", "79000")
                        .param("stock", "5")
                        .param("salesCount", "10")
                        .param("petType", "CAT")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        assertThat(productRepository.findAll())
                .extracting(ProductEntity::getProductName)
                .contains("캣타워");
    }
}
