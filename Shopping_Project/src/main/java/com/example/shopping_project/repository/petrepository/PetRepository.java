package com.example.shopping_project.repository.petrepository;

import com.example.shopping_project.entity.petentity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

    // 회원 PK(member_id)로 반려동물 목록 찾기
    List<PetEntity> findByMemberId(Long memberId);
}