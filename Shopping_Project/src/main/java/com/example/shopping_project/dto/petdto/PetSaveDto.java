package com.example.shopping_project.dto.petdto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PetSaveDto {

    private String petType;      // DOG 또는 CAT
    private String name;         // 이름
    private String breed;        // 견종 / 묘종
    private LocalDate birthDate; // 생일
    private String gender;       // MALE 또는 FEMALE
    private Boolean isNeutered;  // 중성화 여부
    private Double weight;       // 몸무게
    private Boolean hasAllergy;  // 알러지 유무
    private List<String> allergies; // 체크박스 다중 선택 알러지 목록

}