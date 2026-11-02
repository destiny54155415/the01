package com.example.board.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(assignableTypes = PostController.class)
public class PageExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public String notFound(ResponseStatusException exception, Model model) {
        model.addAttribute("message", exception.getReason() == null ? "글을 찾을 수 없습니다." : exception.getReason());
        return "error";
    }
}
