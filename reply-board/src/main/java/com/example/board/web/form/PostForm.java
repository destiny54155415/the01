package com.example.board.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 200, message = "제목은 200자 이하로 입력하세요.")
    private String title;

    @NotBlank(message = "작성자를 입력하세요.")
    @Size(max = 30, message = "작성자는 30자 이하로 입력하세요.")
    private String writer;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 4000, message = "내용은 4000자 이하로 입력하세요.")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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
}
