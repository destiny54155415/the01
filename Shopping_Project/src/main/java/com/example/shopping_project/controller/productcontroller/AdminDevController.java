package com.example.shopping_project.controller.productcontroller;

import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로컬 관리자 상품 화면 진입점.
 * 회원팀 클래스에 컴파일 의존하지 않고, 런타임 리플렉션으로 loginUser 만 맞춘다.
 */
@Controller
public class AdminDevController {

    private static final String DEFAULT_ADMIN_USERNAME = "destiny66";
    private static final String MEMBER_REPOSITORY_CLASS =
            "com.example.shopping_project.repository.memberrepository.MemberRepository";

    private final ApplicationContext applicationContext;

    public AdminDevController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/admin/dev-login")
    public String enableAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("adminDev", true);
        session.setAttribute("loginRole", "ADMIN");
        session.setAttribute("loginUsername", "admin");
        session.setAttribute("loginMemberName", "관리자");

        Object adminMember = resolveAdminMember();
        if (adminMember != null) {
            // Cart/Order 컨트롤러가 session loginUser 를 MemberEntity 로 사용
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
                        ? "로컬 관리자 모드로 들어왔습니다."
                        : "로컬 관리자 모드로 들어왔습니다. (DB 관리자 없음 → 장바구니/주문은 일반 로그인 필요)");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/dev-logout")
    public String disableAdmin(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private Object resolveAdminMember() {
        Object repository = findMemberRepository();
        if (repository == null) {
            return null;
        }

        Object byUsername = unwrap(invoke(repository, "findByUsername", DEFAULT_ADMIN_USERNAME));
        if (byUsername != null) {
            return byUsername;
        }

        Object all = invoke(repository, "findAll");
        if (all instanceof Collection<?> members) {
            for (Object member : members) {
                if (isAdminRole(invokeString(member, "getRole"))) {
                    return member;
                }
            }
        }
        return null;
    }

    private Object findMemberRepository() {
        try {
            Class<?> repoType = Class.forName(MEMBER_REPOSITORY_CLASS);
            Map<String, ?> beans = applicationContext.getBeansOfType(repoType);
            if (!beans.isEmpty()) {
                return beans.values().iterator().next();
            }
        } catch (ClassNotFoundException | RuntimeException ignored) {
            // 회원 모듈이 아직 없거나 bean 이름이 다른 경우
        }
        return null;
    }

    private static Object unwrap(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return result;
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
        return null;
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
