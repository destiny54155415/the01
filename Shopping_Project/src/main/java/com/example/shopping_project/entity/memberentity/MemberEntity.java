package com.example.shopping_project.entity.memberentity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "members") // DB 테이블명 매칭
@Entity
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id") // DB 컬럼명 매칭
    private Long id;

    @Column(unique = true, nullable = false, length = 50) // 중복 값X, 빈 값X
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String zipcode;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    // 회원가입
    public MemberEntity(String username, String password, String name, String email, String phone) {
      this.username = username;
      this.password = password;
      this.name = name;
      this.email = email;
      this.phone = phone;
      this.role = "USER";
    }

    // 회원정보 수정
    public void updateInfo(String email, String phone,
                           String zipcode, String address, String addressDetail) {
        this.email = email;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    // 비밀번호 수정
    public void changePassword(String password) {
        this.password = password;
    }
}
