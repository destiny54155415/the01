package com.example.shopping_project.controller.productcontroller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원 로그인 연동 전, 로컬에서 관리자 상품 화면을 확인하기 위한 임시 진입점입니다.
 */
@Controller
public class AdminDevController {

    @GetMapping("/admin/dev-login")
    public String enableAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("adminDev", true);
        session.setAttribute("loginRole", "ADMIN");
        redirectAttributes.addFlashAttribute("message", "로컬 관리자 모드로 들어왔습니다.");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/dev-logout")
    public String disableAdmin(HttpSession session) {
        session.removeAttribute("adminDev");
        session.removeAttribute("loginRole");
        return "redirect:/";
    }
}
