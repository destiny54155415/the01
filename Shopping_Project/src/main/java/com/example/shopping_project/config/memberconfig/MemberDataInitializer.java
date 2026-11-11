package com.example.shopping_project.config.memberconfig;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 시작 시 기본 관리자 계정이 없으면 MySQL(또는 사용 중 DB)에 자동 저장한다.
 * 로그인: destiny66 / 1234
 */
@Component
@Profile("!test")
public class MemberDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MemberDataInitializer.class);

    public static final String DEFAULT_ADMIN_USERNAME = "destiny66";
    public static final String DEFAULT_ADMIN_PASSWORD = "1234";
    public static final String DEFAULT_ADMIN_EMAIL = "destiny66@petshop.local";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberDataInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureDefaultAdmin();
    }

    private void ensureDefaultAdmin() {
        memberRepository.findByUsername(DEFAULT_ADMIN_USERNAME).ifPresentOrElse(existing -> {
            if (!existing.isAdmin()) {
                existing.changeRole("ADMIN");
                log.info("기본 계정 role 을 ADMIN 으로 복구: {}", DEFAULT_ADMIN_USERNAME);
            }
        }, () -> {
            if (memberRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
                log.warn("기본 관리자 이메일이 이미 있어 계정 자동 생성을 건너뜁니다: {}", DEFAULT_ADMIN_EMAIL);
                return;
            }
            MemberEntity admin = new MemberEntity(
                    DEFAULT_ADMIN_USERNAME,
                    passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                    "관리자",
                    DEFAULT_ADMIN_EMAIL,
                    "01000000000"
            );
            admin.changeRole("ADMIN");
            memberRepository.save(admin);
            log.info("기본 관리자 계정을 DB에 저장했습니다: {} / {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        });
    }
}
