
package com.example.shopping_project.controller.addresscontroller;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.addressservice.AddressService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.shopping_project.entity.addressentity.AddressEntity;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/address")
public class AddressController {

    private final AddressService addressService;


    // 배송지 추가
    @PostMapping("/add")
    public String addAddress(
            @RequestParam String addressName,
            @RequestParam String recipient,
            @RequestParam String phone,
            @RequestParam(required = false) String zipcode,
            @RequestParam String address,
            @RequestParam(required = false) String addressDetail,
            @RequestParam(defaultValue = "false") boolean makeDefault,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        addressService.addAddress(
                loginUser.getId(),
                addressName,
                recipient,
                phone,
                zipcode,
                address,
                addressDetail,
                makeDefault
        );

        return "redirect:/mypage/address";
    }

    // 주문서에서 AJAX로 배송지 등록
    @PostMapping("/add-ajax")
    @ResponseBody
    public Map<String, Object> addAddressAjax(
            @RequestParam String addressName,
            @RequestParam String recipient,
            @RequestParam String phone,
            @RequestParam(required = false) String zipcode,
            @RequestParam String address,
            @RequestParam(required = false) String addressDetail,
            @RequestParam(defaultValue = "false") boolean makeDefault,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        Map<String, Object> response = new LinkedHashMap<>();

        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            AddressEntity savedAddress = addressService.addAddress(
                    loginUser.getId(),
                    addressName,
                    recipient,
                    phone,
                    zipcode,
                    address,
                    addressDetail,
                    makeDefault
            );

            response.put("success", true);
            response.put("id", savedAddress.getId());
            response.put("addressName", savedAddress.getAddressName());
            response.put("recipient", savedAddress.getRecipient());
            response.put("phone", savedAddress.getPhone());
            response.put("zipcode", savedAddress.getZipcode());
            response.put("address", savedAddress.getAddress());
            response.put("addressDetail", savedAddress.getAddressDetail());
            response.put("isDefault", savedAddress.isDefault());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "배송지 등록에 실패했습니다.");
        }

        return response;
    }

    // 배송지 수정
    @PostMapping("/edit")
    public String editAddress(
            @RequestParam Long addressId,
            @RequestParam String addressName,
            @RequestParam String recipient,
            @RequestParam String phone,
            @RequestParam(required = false) String zipcode,
            @RequestParam String address,
            @RequestParam(required = false) String addressDetail,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        addressService.updateAddress(
                loginUser.getId(),
                addressId,
                addressName,
                recipient,
                phone,
                zipcode,
                address,
                addressDetail
        );

        return "redirect:/mypage/address";
    }

    // 배송지 삭제
    @PostMapping("/delete")
    public String deleteAddress(
            @RequestParam Long addressId,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        addressService.deleteAddress(
                loginUser.getId(),
                addressId
        );

        return "redirect:/mypage/address";
    }

    // 기본 배송지 설정
    @PostMapping("/default")
    public String setDefaultAddress(
            @RequestParam Long addressId,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        addressService.setDefaultAddress(
                loginUser.getId(),
                addressId
        );

        return "redirect:/mypage/address";
    }
}
