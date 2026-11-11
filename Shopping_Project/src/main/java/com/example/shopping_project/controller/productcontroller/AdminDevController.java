package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로컬에서 관리자 상품 화면을 확인하기 위한 임시 진입점입니다.
 * 장바구니/주문은 loginUser(MemberEntity)가 필요하므로 DB 관리자 계정도 함께 세션에 넣습니다.
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

        // CartItemController / OrderController 는 session loginUser(MemberEntity) 를 요구함
        MemberEntity adminMember = memberRepository.findByUsername(DEFAULT_ADMIN_USERNAME)
                .or(() -> memberRepository.findAll().stream()
                        .filter(MemberEntity::isAdmin)
                        .findFirst())
                .orElse(null);
        if (adminMember != null) {
            session.setAttribute("loginUser", adminMember);
            session.setAttribute("loginMemberName", adminMember.getName());
            session.setAttribute("loginUsername", adminMember.getUsername());
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
}
