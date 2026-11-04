package com.example.shopping_project.controller.membercontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 회원 담당 영역 (화면 경로만 맞춤 — 상세 로직은 회원 담당)
 */
@Controller
public class MemberController {

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }
}
