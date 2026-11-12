
package com.example.shopping_project.repository.addressrepository;

import com.example.shopping_project.entity.addressentity.AddressEntity;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    // 회원의 배송지 전체 조회
    List<AddressEntity> findAllByMemberOrderByIsDefaultDescIdDesc(MemberEntity member);

    // 회원의 기본 배송지 조회
    Optional<AddressEntity> findByMemberAndIsDefaultTrue(MemberEntity member);

    // 회원 소유의 특정 배송지 조회
    Optional<AddressEntity> findByIdAndMember(Long id, MemberEntity member);

    // 회원의 배송지 개수 확인
    long countByMember(MemberEntity member);

    // 회원의 기본 배송지 개수 확인
    long countByMemberAndIsDefaultTrue(MemberEntity member);
}
