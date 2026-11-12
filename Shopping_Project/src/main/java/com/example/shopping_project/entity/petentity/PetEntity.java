package com.example.shopping_project.entity.petentity;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Table(name = "pets") // DB 테이블명 매칭
@Entity
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id") // DB PK 컬럼명 매칭
    private Long id;

    // 회원이랑 1:N 관계 매칭 (어떤 회원의 펫인지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "pet_type", nullable = false, length = 10)
    private String petType; // 'DOG' 또는 'CAT'

    @Column(nullable = false, length = 50)
    private String name; // 반려동물 이름

    @Column(length = 50)
    private String breed; // 견종 / 묘종

    @Column(name = "birth_date")
    private LocalDate birthDate; // 생일

    @Column(nullable = false, length = 10)
    private String gender; // 'MALE' 또는 'FEMALE'

    @Column(name = "is_neutered", nullable = false)
    private Boolean isNeutered; // 중성화 여부 (true / false)

    @Column
    private Double weight; // 몸무게 (kg)

    @Column(name = "has_allergy", nullable = false)
    private Boolean hasAllergy; // 알러지 유무 (true / false)

    @Column(name = "allergy_info", length = 255)
    private String allergyInfo; // 콤마로 결합된 알러지 상세 내용 ("닭고기,연어")

    // 반려동물 등록용 생성자
    public PetEntity(MemberEntity member, String petType, String name, String breed,
                     LocalDate birthDate, String gender, Boolean isNeutered,
                     Double weight, Boolean hasAllergy, String allergyInfo) {
        this.member = member;
        this.petType = petType;
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isNeutered = isNeutered;
        this.weight = weight;
        this.hasAllergy = hasAllergy;
        this.allergyInfo = allergyInfo;
    }
}
