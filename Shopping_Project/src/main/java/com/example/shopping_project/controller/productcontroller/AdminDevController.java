package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로컬에서 관리자 상품 화면을 확인하기 위한 임시 진입점입니다.
 * 장바구니/주문은 loginUser(MemberEntity)가 필요하므로 DB 관리자 계정도 함께 세션에 넣습니다.
 * MemberEntity API 차이에 의존하지 않도록 getRole() 문자열만 사용합니다.
 */
@Controller
public class AdminDevController {

    private static final String DEFAULT_ADMIN_USERNAME = "destiny66";

    private final MemberRepository memberRepository;

    public AdminDevController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/admin/dev-login")
    public String enableAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("adminDev", true);
        session.setAttribute("loginRole", "ADMIN");
        session.setAttribute("loginUsername", "admin");
        session.setAttribute("loginMemberName", "관리자");

        MemberEntity adminMember = resolveAdminMember();
        if (adminMember != null) {
            session.setAttribute("loginUser", adminMember);
            String name = safeName(adminMember);
            if (name != null) {
                session.setAttribute("loginMemberName", name);
            }
            String username = safeUsername(adminMember);
            if (username != null) {
                session.setAttribute("loginUsername", username);
            }
        }

        redirectAttributes.addFlashAttribute("message",
                adminMember != null
                        ? "로컬 관리자 모드로 들어왔습니다. 장바구니/주문내역도 회원 세션으로 확인 가능합니다."
                        : "로컬 관리자 모드로 들어왔습니다. (DB 관리자 계정 없음 → 장바구니/주문은 일반 로그인 필요)");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/dev-logout")
    public String disableAdmin(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private MemberEntity resolveAdminMember() {
        Optional<MemberEntity> byUsername = memberRepository.findByUsername(DEFAULT_ADMIN_USERNAME);
        if (byUsername.isPresent()) {
            return byUsername.get();
        }

        List<MemberEntity> members = memberRepository.findAll();
        for (MemberEntity member : members) {
            if (isAdminRole(safeRole(member))) {
                return member;
            }
        }
        return null;
    }

    private static boolean isAdminRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return "ADMIN".equals(normalized);
    }

    private static String safeRole(MemberEntity member) {
        try {
            Object role = member.getClass().getMethod("getRole").invoke(member);
            return role != null ? String.valueOf(role) : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String safeName(MemberEntity member) {
        try {
            Object name = member.getClass().getMethod("getName").invoke(member);
            if (name instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return null;
    }

    private static String safeUsername(MemberEntity member) {
        try {
            Object username = member.getClass().getMethod("getUsername").invoke(member);
            if (username instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }
        return null;
    }
}
