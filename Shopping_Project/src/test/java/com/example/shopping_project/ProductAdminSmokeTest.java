package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.repository.productrepository.CategoryRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductAdminSmokeTest {

    @Autowired MockMvc mockMvc;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginRole", "ADMIN");
        session.setAttribute("adminDev", true);
        session.setAttribute("loginMemberName", "관리자");
        return session;
    }

    @Test
    void productCreateFormRenders() throws Exception {
        mockMvc.perform(get("/admin/products/new").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("product/admin-form"));
    }

    @Test
    void productCanBeCreatedWithOptionalImageUpload() throws Exception {
        CategoryEntity food = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(new CategoryEntity("사료", 1)));

        MockMultipartFile image = new MockMultipartFile(
                "imageFile",
                "dog-chicken.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        mockMvc.perform(multipart("/admin/products")
                        .file(image)
                        .session(adminSession())
                        .param("categoryId", String.valueOf(food.getCategoryId()))
                        .param("productName", "강아지용 닭고기 사료")
                        .param("description", "닭고기 사료")
                        .param("price", "19900")
                        .param("stock", "10")
                        .param("salesCount", "0")
                        .param("petType", "DOG")
                        .param("allergenLabels", "닭고기")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));

        assertThat(productRepository.findAll())
                .anyMatch(p -> "강아지용 닭고기 사료".equals(p.getProductName())
                        && p.getImageUrl() != null
                        && p.getImageUrl().startsWith("/uploads/products/"));
    }
}
