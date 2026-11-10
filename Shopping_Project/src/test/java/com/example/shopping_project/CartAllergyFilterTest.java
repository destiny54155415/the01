package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shopping_project.dto.petdto.PetSaveDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import com.example.shopping_project.repository.productrepository.CategoryRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import com.example.shopping_project.service.cartitem.CartItemService;
import com.example.shopping_project.service.petservice.PetService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartAllergyFilterTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemService cartItemService;
    @Autowired PetService petService;

    MemberEntity member;
    ProductEntity chickenProduct;
    ProductEntity safeProduct;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new MemberEntity(
                "allergy-user",
                passwordEncoder.encode("1234"),
                "테스터",
                "allergy@test.local",
                null
        ));
        CategoryEntity food = categoryRepository.save(new CategoryEntity("사료", 1));
        chickenProduct = productRepository.save(new ProductEntity(
                food, "강아지 닭고기 사료", "닭고기 포함", new BigDecimal("19900"), 10, PetType.DOG, "/images/food-dog.jpg"));
        chickenProduct.setAllergens("닭고기");
        safeProduct = productRepository.save(new ProductEntity(
                food, "감자 간식", "채소 간식", new BigDecimal("9900"), 10, PetType.DOG, "/images/treat.jpg"));
        safeProduct.setAllergens("감자");
    }

    @Test
    void cartHidesProductsMatchingPetAllergyByDefault() throws Exception {
        PetSaveDto pet = new PetSaveDto();
        pet.setName("초코");
        pet.setPetType("DOG");
        pet.setAllergies(List.of("닭고기"));
        petService.registerPet(member.getId(), pet);

        cartItemService.addCart(member.getId(), chickenProduct.getProductId(), 1);
        cartItemService.addCart(member.getId(), safeProduct.getProductId(), 1);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", member);

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/list"))
                .andExpect(model().attribute("filteredOutCount", 1))
                .andExpect(model().attribute("allergyLabelText", "닭고기"));

        var result = mockMvc.perform(get("/cart").session(session)).andReturn();
        @SuppressWarnings("unchecked")
        var visible = (java.util.List<?>) result.getModelAndView().getModel().get("cartItems");
        assertThat(visible).hasSize(1);
    }

    @Test
    void showUnsafeIncludesAllergyProducts() throws Exception {
        PetSaveDto pet = new PetSaveDto();
        pet.setName("초코");
        pet.setAllergies(List.of("닭고기"));
        petService.registerPet(member.getId(), pet);
        cartItemService.addCart(member.getId(), chickenProduct.getProductId(), 1);
        cartItemService.addCart(member.getId(), safeProduct.getProductId(), 1);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", member);

        var result = mockMvc.perform(get("/cart").param("showUnsafe", "true").session(session))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        var visible = (java.util.List<?>) result.getModelAndView().getModel().get("cartItems");
        assertThat(visible).hasSize(2);
    }

    @Test
    void petFormRegistersAllergySelection() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", member);

        mockMvc.perform(post("/mypage/pets")
                        .session(session)
                        .param("petType", "DOG")
                        .param("name", "콩이")
                        .param("allergies", "닭고기")
                        .param("allergies", "우유"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/pets"));

        assertThat(petService.getAllergyLabelsForMember(member.getId()))
                .containsExactlyInAnyOrder("닭고기", "우유");
    }
}
