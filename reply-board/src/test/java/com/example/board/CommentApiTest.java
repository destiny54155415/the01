package com.example.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.board.repository.CommentRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CommentRepository comments;

    @Test
    void commentWithoutReloadingTheDetailPage() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "게시판 글 제목")
                        .param("writer", "foxman12")
                        .param("content", "게시판 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("댓글을 남겨보세요")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/js/comments.js")));

        MvcResult created = mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"writer":"foxman12","content":"Rest Api를 이용한 댓글 추가"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.writer").value("foxman12"))
                .andReturn();
        Number commentId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"writer":"foxman12","content":"답글입니다","parentId":%d}
                                """.formatted(commentId.longValue())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(commentId.longValue()));

        mockMvc.perform(get("/api/posts/1/comments").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Rest Api를 이용한 댓글 추가"))
                .andExpect(jsonPath("$[0].replies[0].content").value("답글입니다"));

        mockMvc.perform(put("/api/comments/" + commentId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정된 댓글\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글"));

        mockMvc.perform(post("/api/posts/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1));

        mockMvc.perform(delete("/api/comments/" + commentId.longValue()))
                .andExpect(status().isNoContent());
        assertThat(comments.count()).isZero();

        mockMvc.perform(post("/posts/1/reply")
                        .param("title", "RE: 게시판 글 제목")
                        .param("writer", "foxman12")
                        .param("content", "답글 본문"))
                .andExpect(redirectedUrlPattern("/posts/*"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("RE: 게시판 글 제목")));
    }
}
