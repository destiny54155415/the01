package com.example.shopping_project.service.petservice;

import java.util.List;
import com.example.shopping_project.dto.petdto.PetSaveDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.petentity.PetEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import com.example.shopping_project.repository.petrepository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;

    // 반려동물 등록
    @Transactional
    public void registerPet(String username, PetSaveDto dto) {

        // 회원 존재 여부 확인
        MemberEntity member = memberRepository.findByUsername(username);

        if(member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 체크박스(allergies)가 하나라도 체크되어 있다면 true, 아니면 false
        boolean calculatedHasAllergy = (dto.getAllergies() != null && !dto.getAllergies().isEmpty());

        // 선택된 알러지가 있다면 콤마로 결합, 없으면 null
        String allergyInfoResult = calculatedHasAllergy ? String.join(",", dto.getAllergies()) : null;

        // 반려동물 엔티티 생성
        PetEntity petEntity = new PetEntity(
                member,
                dto.getPetType(),
                dto.getName(),
                dto.getBreed(),
                dto.getBirthDate(),
                dto.getGender(),
                dto.getIsNeutered(),
                dto.getWeight(),
                calculatedHasAllergy,
                allergyInfoResult
        );

        // DB에 반려동물 저장
        petRepository.save(petEntity);
    }

    // 회원의 반려동물 목록 조회
    public List<PetEntity> getPetsByMember(Long memberId) {
        return petRepository.findByMemberId(memberId);
    }
}