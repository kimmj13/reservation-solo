package com.bit.reservation.global.security.utils;

import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomAuthorityUtils {

    private final HospitalRepository hospitalRepository;
    @Value("${mail.address.admin}")
    private String adminMailAddress;
    private final List<String> ADMIN_ROLES_STRING = List.of("ADMIN", "USER");
    private final List<String> USER_ROLES_STRING = List.of("USER");
    private final List<String> HOSPITAL_ROLES_STRING = List.of("HOSPITAL");
    private final List<String> OK_HOSPITAL_ROLES_STRING = List.of("HOSPITAL", "OK");


    // DB에 저장된 Role을 기반으로 권한 정보 생성
    public List<GrantedAuthority> createAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return authorities;
    }

    // DB 저장 용
    public List<String> createRoles(String email, boolean isHospital) {
        if (email.equals(adminMailAddress)) {
            return ADMIN_ROLES_STRING;
        } else if (isHospital) {
            return HOSPITAL_ROLES_STRING;
        }
        return USER_ROLES_STRING;
    }

    public List<String> acceptedHospital() {
        return OK_HOSPITAL_ROLES_STRING;
    }

    public List<String> getHospitalRoles(String email) {
        return hospitalRepository.findByEmail(email).orElse(null).getRoles();
    }
}
