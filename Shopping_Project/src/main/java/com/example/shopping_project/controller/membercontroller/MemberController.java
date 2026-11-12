package com.example.shopping_project.controller.membercontroller;

import com.example.shopping_project.dto.memberdto.MemberJoinDto;
import com.example.shopping_project.dto.memberdto.MemberLoginDto;
import com.example.shopping_project.dto.order.OrderResponseDTO;
import com.example.shopping_project.dto.petdto.PetSaveDto;
import com.example.shopping_project.dto.review.ReviewResponseDTO;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.petentity.PetEntity;
import com.example.shopping_project.service.addressservice.AddressService;
import com.example.shopping_project.service.memberservice.MemberService;
import com.example.shopping_project.service.order.OrderService;
import com.example.shopping_project.service.petservice.PetService;
import com.example.shopping_project.service.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MemberController {
    private final MemberService memberService;
    private final ReviewService reviewService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final PetService petService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupForm() {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(MemberJoinDto dto, Model model) {

        try {
            memberService.join(dto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/signup";
        }

        return "redirect:/login";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(MemberLoginDto dto, HttpSession session, Model model) {
        MemberEntity member = memberService.login(dto);

        if (member == null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다. 입력한 정보를 다시 확인해 주세요.");
            return "member/login";
        }

        session.setAttribute("loginUser", member);

        return "redirect:/";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();

        return "redirect:/";
    }

    // 마이페이지 이동
    @GetMapping("/mypage")
    public String mypage(HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        return "member/mypage";
    }

//    @GetMapping("/mypage")
//    public String mypage(HttpSession session, Model model) {
//        Object loginUser = session.getAttribute("loginUser");
//
//        if(loginUser==null) {
//            return "redirect:/login";
//        }
//
//        model.addAttribute("member", loginUser);;
//        return "member/mypage";
//    }

    // 회원 탈퇴 처리
    @PostMapping("/delete-member")
    public String deleteMember(HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser != null) {
            memberService.deleteMember(loginUser.getUsername());
            session.invalidate();
        }

        return "redirect:/";
    }

    // 회원정보 수정
    @GetMapping("/mypage/edit")
    public String editForm(HttpSession session, Model model) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("member", loginUser);
        return "member/edit";
    }

    @PostMapping("/mypage/edit")
    public String updateMember(String password, String email, String phone,
                               String zipcode, String address, String addressDetail,
                               HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser != null) {

            // 회원 정보 수정
            memberService.updateMember(
                    loginUser.getUsername(),
                    password,
                    email,
                    phone,
                    zipcode,
                    address,
                    addressDetail
            );

            // 수정된 회원 정보를 다시 조회
            MemberEntity updateUser = memberService.findByUsername(loginUser.getUsername());

            // 세션에 최신 회원 정보 저장
            session.setAttribute("loginUser", updateUser);
        }

        return "redirect:/mypage/member";
    }

    // ID 중복 확인 메세지
    @GetMapping("/members/check-id")
    @ResponseBody
    public boolean checkId(@RequestParam("username") String username) {
        return memberService.checkUsernameDuplicate(username);

    }

    // 회원 정보 조회 페이지
    @GetMapping("/mypage/member")
    public String memberInfo(HttpSession session, Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("member", loginUser);

        return "member/member-info";
    }

    // 상품평 관리
    @GetMapping("/mypage/reviews")
    public String reviewManagement(HttpSession session, Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        List<ReviewResponseDTO> reviews =
                reviewService.getMyReviewList(loginUser.getId());

        model.addAttribute("reviews", reviews);
        model.addAttribute("activeTab", "reviews");

        return "member/mypage";
    }

    // 1106 위시리스트 연결 경로 추가
    @GetMapping("/mypage/wishlist")
    public String mypageWishlistForward() {
        return "forward:/wishlist";
    }

    // 마이페이지 주문목록 / 배송조회
    @GetMapping("/mypage/order")
    public String orderList(HttpSession session, Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        List<OrderResponseDTO> orders =
                orderService.getOrderList(loginUser.getId());

        model.addAttribute("orders", orders);
        model.addAttribute("activeTab", "order");

        return "member/mypage";
    }

    // 내 반려동물 메뉴 추가_261109
    @GetMapping("/mypage/pets")
    public String petList(HttpSession session, Model model) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if(loginUser == null) {
            return "redirect:/login";
        }

        List<PetEntity> pets = petService.getPetsByMember(loginUser.getId());
        model.addAttribute("pets", pets);

        return "pet/pet-list";
    }

    // 마이페이지 배송지 관리
    @GetMapping("/mypage/address")
    public String addressManagement(HttpSession session, Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("addresses",
                addressService.getAddressList(loginUser.getId()));

        model.addAttribute("activeTab", "address");

        return "member/mypage";
    }

    // 반려동물 등록 페이지 이동
    @GetMapping("/mypage/pets/new")
    public String petForm(HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if(loginUser == null) {
            return "redirect:/login";
        }

        return "pet/pet-form";
    }

    // 반려동물 등록 저장
    @PostMapping("/mypage/pets/add")
    public String addPet(PetSaveDto dto, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if(loginUser == null) {
            return "redirect:/login";
        }

        petService.registerPet(loginUser.getUsername(), dto);

        return "redirect:/mypage/pets";
    }
}



