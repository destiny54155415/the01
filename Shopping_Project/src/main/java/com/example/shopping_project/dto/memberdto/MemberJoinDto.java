package com.example.shopping_project.dto.memberdto;

import lombok.Data;

@Data
public class MemberJoinDto {

    private String username; // 아이디
    private String password; // 비밀번호
    private String name; // 이름
    private String email; // 이메일
    private String phone; // 전화번호

}
