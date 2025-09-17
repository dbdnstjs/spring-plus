package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(Long id, String email, String nickname,  UserRole userRole) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    // UserRole을 직접 꺼낼 수 있도록 메서드 추가
    public UserRole getUserRole() {
        return authorities.stream()
                .findFirst()
                .map(auth -> UserRole.valueOf(auth.getAuthority()))
                .orElseThrow(() -> new IllegalStateException("권한이 없습니다."));
    }
}
