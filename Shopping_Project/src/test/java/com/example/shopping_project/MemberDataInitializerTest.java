package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shopping_project.config.memberconfig.MemberDataInitializer;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberDataInitializerTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void createsDefaultAdminWhenMissing() throws Exception {
        assertThat(memberRepository.existsByUsername(MemberDataInitializer.DEFAULT_ADMIN_USERNAME)).isFalse();

        new MemberDataInitializer(memberRepository, passwordEncoder).run();

        MemberEntity admin = memberRepository.findByUsername(MemberDataInitializer.DEFAULT_ADMIN_USERNAME)
                .orElseThrow();
        assertThat(admin.isAdmin()).isTrue();
        assertThat(passwordEncoder.matches(
                MemberDataInitializer.DEFAULT_ADMIN_PASSWORD,
                admin.getPassword())).isTrue();
    }

    @Test
    void restoresAdminRoleIfUserExistsWithoutAdmin() throws Exception {
        MemberEntity user = new MemberEntity(
                MemberDataInitializer.DEFAULT_ADMIN_USERNAME,
                passwordEncoder.encode("other"),
                "일반",
                "other-" + MemberDataInitializer.DEFAULT_ADMIN_EMAIL,
                null
        );
        memberRepository.save(user);

        new MemberDataInitializer(memberRepository, passwordEncoder).run();

        assertThat(memberRepository.findByUsername(MemberDataInitializer.DEFAULT_ADMIN_USERNAME)
                .orElseThrow()
                .isAdmin()).isTrue();
    }
}
