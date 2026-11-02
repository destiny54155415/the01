package com.example.customerorder.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerForm {

    @NotBlank(message = "고객명을 입력하세요.")
    @Size(max = 100, message = "고객명은 100자 이하로 입력하세요.")
    private String name;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 아닙니다.")
    @Size(max = 255, message = "이메일은 255자 이하로 입력하세요.")
    private String email;

    @Size(max = 30, message = "연락처는 30자 이하로 입력하세요.")
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
