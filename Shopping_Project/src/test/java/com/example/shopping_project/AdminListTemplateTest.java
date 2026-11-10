
package com.example.shopping_project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminListTemplateTest {
    @Autowired MockMvc mockMvc;

    @Test
    void adminListRenders() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginRole", "ADMIN");
        mockMvc.perform(get("/admin/products").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("product/admin-list"));
    }
}
