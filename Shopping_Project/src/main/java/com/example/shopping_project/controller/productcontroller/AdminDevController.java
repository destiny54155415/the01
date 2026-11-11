package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로컬에서 관리자 상품 화면을 확인하기 위한 임시 진입점입니다.
 * 회원팀 Repository/Entity API 차이(Optional 여부, isAdmin 유무)에 의존하지 않습니다.
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
            String name = invokeString(adminMember, "getName");
            if (name != null) {
                session.setAttribute("loginMemberName", name);
            }
            String username = invokeString(adminMember, "getUsername");
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
        MemberEntity byUsername = findByUsernameCompat(DEFAULT_ADMIN_USERNAME);
        if (byUsername != null) {
            return byUsername;
        }

        Object all = invoke(memberRepository, "findAll");
        if (all instanceof Collection<?> members) {
            for (Object member : members) {
                if (member instanceof MemberEntity entity && isAdminRole(invokeString(entity, "getRole"))) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * 회원팀 findByUsername 이 Optional&lt;MemberEntity&gt; 또는 MemberEntity(null 가능) 모두 대응.
     */
    private MemberEntity findByUsernameCompat(String username) {
        Object result = invoke(memberRepository, "findByUsername", username);
        if (result == null) {
            return null;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            return value instanceof MemberEntity entity ? entity : null;
        }
        if (result instanceof MemberEntity entity) {
            return entity;
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

    private static String invokeString(Object target, String methodName) {
        Object value = invoke(target, methodName);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        return value != null ? String.valueOf(value) : null;
    }

    private static Object invoke(Object target, String methodName, Object... args) {
        if (target == null) {
            return null;
        }
        try {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                if (method.getParameterCount() != args.length) {
                    continue;
                }
                return method.invoke(target, args);
            }
        } catch (ReflectiveOperationException ignored) {
            // 회원팀 API 차이 무시
        }
        return null;
    }
}
