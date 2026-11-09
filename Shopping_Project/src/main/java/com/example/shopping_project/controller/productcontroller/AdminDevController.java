package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.memberservice.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로컬에서 관리자 상품 화면을 확인하기 위한 임시 진입점입니다.
 */
@Controller
public class AdminDevController {

    private final MemberService memberService;

    public AdminDevController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/dev-login")
    public String enableAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("adminDev", true);
        session.setAttribute("loginRole", "ADMIN");
        redirectAttributes.addFlashAttribute("message", "로컬 관리자 모드로 들어왔습니다.");
        return "redirect:/admin/products";
    }

    /**
     * DB members.role 을 ADMIN 으로 올리고, 현재 세션에도 반영합니다.
     * 예: /admin/make-admin?username=destiny66
     */
    @GetMapping("/admin/make-admin")
    public String makeAdmin(@RequestParam String username,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            MemberEntity member = memberService.grantAdmin(username);
            session.setAttribute("loginMember", member);
            session.setAttribute("loginUser", member);
            session.setAttribute("loginMemberId", member.getId());
            session.setAttribute("loginUsername", member.getUsername());
            session.setAttribute("loginMemberName", member.getName());
            session.setAttribute("loginRole", "ADMIN");
            session.setAttribute("adminDev", true);
            redirectAttributes.addFlashAttribute(
                    "message",
                    member.getUsername() + " 계정에 ADMIN 권한을 부여했습니다.");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/dev-logout")
    public String disableAdmin(HttpSession session) {
        session.removeAttribute("adminDev");
        session.removeAttribute("loginRole");
        return "redirect:/";
    }
}
