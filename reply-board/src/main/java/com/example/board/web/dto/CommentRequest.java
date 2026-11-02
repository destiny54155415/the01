package com.example.board.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotBlank(message = "작성자를 입력하세요.")
    @Size(max = 30, message = "작성자는 30자 이하로 입력하세요.")
    private String writer;

    @NotBlank(message = "댓글을 입력하세요.")
    @Size(max = 1000, message = "댓글은 1000자 이하로 입력하세요.")
    private String content;

    private Long parentId;

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
