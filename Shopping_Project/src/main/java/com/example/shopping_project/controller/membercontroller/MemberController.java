package com.example.shopping_project.controller.membercontroller;

import com.example.shopping_project.dto.memberdto.MemberJoinDto;
import com.example.shopping_project.dto.memberdto.MemberLoginDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.memberservice.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원 로그인 / 회원가입
 */
@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginDto") MemberLoginDto loginDto) {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginDto") MemberLoginDto loginDto,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            MemberEntity member = memberService.login(loginDto);
            bindLoginSession(session, member);
            redirectAttributes.addFlashAttribute(
                    "loginSuccessMessage",
                    member.getName() + "님, 로그인되었습니다.");
            if (member.isAdmin()) {
                return "redirect:/admin/products";
            }
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "member/login";
        }
    }

    @GetMapping("/signup")
    public String signupForm(@ModelAttribute("joinDto") MemberJoinDto joinDto) {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("joinDto") MemberJoinDto joinDto,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            memberService.join(joinDto);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "member/signup";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private void bindLoginSession(HttpSession session, MemberEntity member) {
        // 엔티티 + 문자열을 함께 넣어 헤더/관리자 체크가 안정적으로 동작하게 함
        String role = member.getRole() == null ? "USER" : member.getRole().trim().toUpperCase();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        session.setAttribute("loginMember", member);
        session.setAttribute("loginUser", member);
        session.setAttribute("loginMemberId", member.getId());
        session.setAttribute("loginUsername", member.getUsername());
        session.setAttribute("loginMemberName", member.getName());
        session.setAttribute("loginRole", role);
        if ("ADMIN".equals(role)) {
            session.setAttribute("adminDev", true);
        }
    }
}
