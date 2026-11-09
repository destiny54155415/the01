package com.example.shopping_project.service.memberservice;

import com.example.shopping_project.dto.memberdto.MemberJoinDto;
import com.example.shopping_project.dto.memberdto.MemberLoginDto;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberEntity join(MemberJoinDto dto) {
        String username = trim(dto.getUsername());
        String password = dto.getPassword();
        String name = trim(dto.getName());
        String email = trim(dto.getEmail());
        String phone = trim(dto.getPhone());

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)
                || !StringUtils.hasText(name) || !StringUtils.hasText(email)) {
            throw new IllegalArgumentException("아이디, 비밀번호, 이름, 이메일은 필수입니다.");
        }
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        MemberEntity member = new MemberEntity(
                username,
                passwordEncoder.encode(password),
                name,
                email,
                phone
        );
        return memberRepository.save(member);
    }

    @Transactional
    public MemberEntity login(MemberLoginDto dto) {
        String username = trim(dto.getUsername());
        String rawPassword = dto.getPassword();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }

        MemberEntity member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) {
            log.warn("로그인 실패: username='{}' 회원이 DB에 없습니다.", username);
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String storedPassword = trim(member.getPassword());
        if (passwordMatches(rawPassword, storedPassword)) {
            upgradePlainPasswordIfNeeded(member, rawPassword);
            return member;
        }

        log.warn("로그인 실패: username='{}' 비밀번호 불일치 (storedPrefix={}, len={})",
                username,
                storedPassword == null ? "null" : storedPassword.substring(0, Math.min(10, storedPassword.length())),
                storedPassword == null ? 0 : storedPassword.length());
        throw new IllegalArgumentException(
                "아이디 또는 비밀번호가 올바르지 않습니다. MySQL에 직접 넣었다면 password를 BCrypt 해시로 바꾸거나 /signup 으로 다시 가입하세요.");
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            try {
                return passwordEncoder.matches(rawPassword, storedPassword);
            } catch (Exception ex) {
                // 잘못된 해시 문자열이 $2a$ 로 시작하는 경우
                log.warn("BCrypt 검증 중 오류: {}", ex.toString());
                return false;
            }
        }
        // Workbench 등에서 평문으로 INSERT 한 기존 계정 호환
        return storedPassword.equals(rawPassword);
    }

    private void upgradePlainPasswordIfNeeded(MemberEntity member, String rawPassword) {
        if (!isBcryptHash(member.getPassword())) {
            member.changePassword(passwordEncoder.encode(rawPassword));
        }
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
