package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberLoginTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void signupThenLoginStoresSession() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "aa")
                        .param("password", "aa")
                        .param("name", "테스트")
                        .param("email", "aa@aa.aa")
                        .param("phone", "01012345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(memberRepository.findByUsername("aa")).isPresent();
        assertThat(memberRepository.findByUsername("aa").orElseThrow().getPassword())
                .startsWith("$2");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", "aa")
                        .param("password", "aa"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("loginSuccessMessage"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("loginMember")).isInstanceOf(MemberEntity.class);
        assertThat(session.getAttribute("loginMemberName")).isEqualTo("테스트");
        assertThat(session.getAttribute("loginRole")).isEqualTo("USER");

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("loggedIn", true))
                .andExpect(model().attribute("loginMemberName", "테스트"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "bb")
                        .param("password", "secret")
                        .param("name", "비비")
                        .param("email", "bb@bb.bb"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/login")
                        .param("username", "bb")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void plainPasswordLegacyAccountCanLoginAndGetsUpgraded() throws Exception {
        memberRepository.save(new MemberEntity("legacy", "plain-pass", "레거시", "legacy@aa.aa", null));

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", "legacy")
                        .param("password", "plain-pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        assertThat(memberRepository.findByUsername("legacy").orElseThrow().getPassword())
                .startsWith("$2");
        assertThat(loginResult.getRequest().getSession(false).getAttribute("loginUser"))
                .isInstanceOf(MemberEntity.class);
    }

    @Test
    void loginPageRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/login"));
    }

    @Test
    void makeAdminGrantsAdminRoleAndSession() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "destiny66")
                        .param("password", "1234")
                        .param("name", "데스티니")
                        .param("email", "destiny66@test.com"))
                .andExpect(status().is3xxRedirection());

        MvcResult result = mockMvc.perform(get("/admin/make-admin")
                        .param("username", "destiny66"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andReturn();

        assertThat(memberRepository.findByUsername("destiny66").orElseThrow().getRole())
                .isEqualTo("ADMIN");
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session.getAttribute("loginRole")).isEqualTo("ADMIN");
        assertThat(session.getAttribute("adminDev")).isEqualTo(true);
    }
}
