package com.example.shopping_project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
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
class RecoverySmokeTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void adminProductFormRenders() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginRole", "ADMIN");
        session.setAttribute("adminDev", true);
        session.setAttribute("loginMemberName", "관리자");

        mockMvc.perform(get("/admin/products/new").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("product/admin-form"));
    }

    @Test
    void cartAndOrdersRequireLoginThenRenderWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/cart")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/orders")).andExpect(redirectedUrl("/login"));

        MemberEntity member = memberRepository.save(new MemberEntity(
                "smoke-user",
                passwordEncoder.encode("1234"),
                "스모크",
                "smoke@test.local",
                null
        ));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", member);

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/list"));

        mockMvc.perform(get("/orders").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order-list"));
    }
}
