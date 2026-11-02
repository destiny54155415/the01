package com.example.customerorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customerorder.repository.CustomerRepository;
import com.example.customerorder.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerOrderWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CustomerRepository customers;

    @Autowired
    OrderRepository orders;

    @Test
    void crudCustomersAndOrders() throws Exception {
        mockMvc.perform(post("/customers")
                        .param("name", "홍길동")
                        .param("email", "hong@test.com")
                        .param("phone", "010-1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/1"));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hong@test.com")));

        mockMvc.perform(post("/customers")
                        .param("name", "김중복")
                        .param("email", "hong@test.com")
                        .param("phone", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 등록된 이메일")));

        mockMvc.perform(post("/orders")
                        .param("customerId", "1")
                        .param("orderNumber", "20261011-0001")
                        .param("totalPrice", "25000")
                        .param("status", "PAID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/1"));

        mockMvc.perform(post("/orders")
                        .param("customerId", "1")
                        .param("orderNumber", "20261011-0002")
                        .param("totalPrice", "18000")
                        .param("status", "PENDING"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("20261011-0001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("20261011-0002")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("홍길동")));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("결제완료")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("대기")));

        mockMvc.perform(get("/orders/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hong@test.com")));

        mockMvc.perform(post("/customers/1")
                        .param("name", "홍길동")
                        .param("email", "hong@test.com")
                        .param("phone", "010-9999-0000"))
                .andExpect(redirectedUrl("/customers/1"));

        assertThat(customers.findByEmail("hong@test.com")).get()
                .extracting("phone").isEqualTo("010-9999-0000");

        mockMvc.perform(post("/orders/1")
                        .param("customerId", "1")
                        .param("orderNumber", "20261011-0001")
                        .param("totalPrice", "30000")
                        .param("status", "SHIPPING"))
                .andExpect(redirectedUrl("/orders"));

        assertThat(orders.findByOrderNumber("20261011-0001")).get()
                .extracting("status").asString().isEqualTo("SHIPPING");

        mockMvc.perform(post("/orders/2/delete"))
                .andExpect(redirectedUrl("/orders"));
        assertThat(orders.findByOrderNumber("20261011-0002")).isEmpty();

        mockMvc.perform(post("/customers/1/delete"))
                .andExpect(redirectedUrl("/customers"));
        assertThat(customers.count()).isZero();
        assertThat(orders.count()).isZero();
    }
}
