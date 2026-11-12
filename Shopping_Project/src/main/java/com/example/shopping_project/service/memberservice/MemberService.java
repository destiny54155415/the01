package com.example.shopping_project.service.memberservice;

import java.util.List;
import com.example.shopping_project.dto.memberdto.MemberJoinDto;
import com.example.shopping_project.dto.memberdto.MemberLoginDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void join(MemberJoinDto dto) {

        // 아이디 중복 검사
        MemberEntity existingMember = memberRepository.findByUsername(dto.getUsername());

        if(existingMember != null) {
            throw new IllegalStateException("이미 사용 중인 ID입니다.");
        }

        // 이메일 중복 검사
        MemberEntity existingEmail = memberRepository.findByEmail(dto.getEmail());

        if(existingEmail != null) {
            throw new IllegalStateException("이미 사용 중인 Email입니다.");
        }

        // 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(dto.getPassword());

        // 회원 객체 생성
        MemberEntity memberEntity = new MemberEntity(
                dto.getUsername(),
                encodePassword,
                dto.getName(),
                dto.getEmail(),
                dto.getPhone()
        );

        // DB에 회원 저장
        memberRepository.save(memberEntity);
    }

    // 로그인
    public MemberEntity login(MemberLoginDto dto) {

        // ID로 회원 검색
        MemberEntity member = memberRepository.findByUsername(dto.getUsername());

        // 회원이 없으면 로그인 X
        if(member == null) {
            return null;
        }

        // 비밀번호 확인
        boolean passwordMatch = passwordEncoder.matches(dto.getPassword(), member.getPassword());

        if(!passwordMatch) {
            return null;
        }

        return member;

    }

    // ID로 회원 찾기
    public MemberEntity findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    // 회원 탈퇴
    public void deleteMember(String username) {
        MemberEntity member = memberRepository.findByUsername(username);
        if(member != null) {
            memberRepository.delete(member);
        }
    }

    // 회원 정보 수정
    public void updateMember(String username, String password, String email,
                             String phone, String zipcode, String address,
                             String addressDetail) {

        MemberEntity member = memberRepository.findByUsername(username);

        if(member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 이메일 중복 검사
        MemberEntity existingEmail = memberRepository.findByEmail(email);

        if(existingEmail != null && !existingEmail.getUsername().equals(username)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // 이메일, 전화번호, 주소 수정
        member.updateInfo(email, phone, zipcode, address, addressDetail);

        // 비밀번호를 입력한 경우에만 변경
        if(password != null && !password.trim().isEmpty()) {

            String encodePassword = passwordEncoder.encode(password);

            member.changePassword(encodePassword);
        }

        // DB에 수정 내용 저장
        memberRepository.save(member);
    }

    // 관리자용 전체 회원 목록 조회
    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }

    // ID 중복 관련 메서드
    public boolean checkUsernameDuplicate(String username) {
        return memberRepository.existsByUsername(username);
    }
}
