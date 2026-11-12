
package com.example.shopping_project.entity.addressentity;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    // 배송지를 소유한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 배송지 이름 (우리집, 회사 등)
    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    // 받는 사람
    @Column(nullable = false, length = 50)
    private String recipient;

    // 연락처
    @Column(nullable = false, length = 20)
    private String phone;

    // 우편번호
    @Column(length = 10)
    private String zipcode;

    // 기본 주소
    @Column(nullable = false, length = 255)
    private String address;

    // 상세 주소
    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    // 기본 배송지 설정
    // 기본 배송지 여부
    @Setter
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    public AddressEntity(
            MemberEntity member,
            String addressName,
            String recipient,
            String phone,
            String zipcode,
            String address,
            String addressDetail,
            boolean isDefault) {

        this.member = member;
        this.addressName = addressName;
        this.recipient = recipient;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.isDefault = isDefault;
    }

    // 배송지 수정
    public void update(
            String addressName,
            String recipient,
            String phone,
            String zipcode,
            String address,
            String addressDetail) {

        this.addressName = addressName;
        this.recipient = recipient;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

}
