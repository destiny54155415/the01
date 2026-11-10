package com.example.shopping_project.controller.petcontroller;

import com.example.shopping_project.dto.petdto.PetSaveDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.AllergyIngredient;
import com.example.shopping_project.service.petservice.PetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 최종본 MemberController 의 /mypage/pets 와 동일한 경로를 제공한다.
 */
@Controller
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/mypage/pets")
    public String petList(HttpSession session, Model model) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("pets", petService.getPetsByMember(loginUser.getId()));
        return "pet/pet-list";
    }

    @GetMapping({"/mypage/pets/new", "/mypage/pet/new"})
    public String petForm(HttpSession session, Model model) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("petForm")) {
            model.addAttribute("petForm", new PetSaveDto());
        }
        model.addAttribute("allergyIngredients", AllergyIngredient.values());
        return "pet/pet-form";
    }

    @PostMapping({"/mypage/pets", "/mypage/pet"})
    public String addPet(@ModelAttribute("petForm") PetSaveDto petForm, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        petService.registerPet(loginUser.getUsername(), petForm);
        return "redirect:/mypage/pets";
    }
}
