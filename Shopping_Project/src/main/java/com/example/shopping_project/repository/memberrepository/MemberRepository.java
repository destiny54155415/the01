package com.example.shopping_project.repository.memberrepository;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    // 아이디로 회원 찾기
    MemberEntity findByUsername(String username);

    // 이메일로 회원 찾기
    MemberEntity findByEmail(String eamil);

    // 아이디 중복 확인
    boolean existsByUsername(String username);
}
