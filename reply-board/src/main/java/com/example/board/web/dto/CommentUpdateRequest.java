package com.example.board.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentUpdateRequest {

    @NotBlank(message = "댓글을 입력하세요.")
    @Size(max = 1000, message = "댓글은 1000자 이하로 입력하세요.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
