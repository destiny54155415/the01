package com.example.shopping_project.controller.membercontroller;

import com.example.shopping_project.service.memberservice.MemberService;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {

    private final MemberService memberService;

    // 관리자 페이지에서 회원관리 메뉴 클릭시 이동
    @GetMapping("/admin/members")
    public String adminMemberList(HttpSession session, Model model) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if(loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/";
        }

        List<MemberEntity> memberList = memberService.getAllMembers();
        model.addAttribute("members", memberList);

        return "admin/member-list";
    }
}
