package com.example.shopping_project.service.petservice;

import com.example.shopping_project.dto.petdto.PetSaveDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.petentity.PetEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import com.example.shopping_project.repository.petrepository.PetRepository;
import com.example.shopping_project.service.productservice.AllergenLabels;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;

    public PetService(PetRepository petRepository, MemberRepository memberRepository) {
        this.petRepository = petRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<PetEntity> getPetsByMember(Long memberId) {
        return petRepository.findByMemberIdOrderByIdAsc(memberId);
    }

    /** 최종 통합본 MemberController 시그니처 호환: username 기준 등록 */
    public void registerPet(String username, PetSaveDto dto) {
        MemberEntity member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        registerPet(member.getId(), dto);
    }

    public PetEntity registerPet(Long memberId, PetSaveDto dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("반려동물 이름을 입력해 주세요.");
        }
        PetEntity pet = new PetEntity(memberId, blankToDefault(dto.getPetType(), "DOG"), dto.getName().trim());
        pet.setBreed(trimToNull(dto.getBreed()));
        pet.setBirthday(dto.getBirthday());
        pet.setGender(blankToDefault(dto.getGender(), "MALE"));
        pet.setNeutered(dto.getNeutered() == null || dto.getNeutered());
        pet.setWeight(dto.getWeight());
        pet.setAllergies(AllergenLabels.join(dto.getAllergies()));
        return petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public Set<String> getAllergyLabelsForMember(Long memberId) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (PetEntity pet : getPetsByMember(memberId)) {
            labels.addAll(AllergenLabels.parse(pet.getAllergies()));
        }
        return labels;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
