package org.example.staystylish.common.security;

import lombok.Getter;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final boolean isNewUser;

    /** Local 로그인용 (JWT 기반) */
    public UserPrincipal(User user) {
        this(user, null, false);
    }

    /** OAuth2 로그인용 (기존 사용자) */
    public UserPrincipal(User user, Map<String, Object> attributes) {
        this(user, attributes, false);
    }

    /** OAuth2 로그인용 (신규 여부 포함) */
    public UserPrincipal(User user, Map<String, Object> attributes, boolean isNewUser) {
        this.user = user;
        this.attributes = attributes;
        this.isNewUser = isNewUser;
    }

    /** 권한 반환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /** UserDetails 구현 */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /** OAuth2User 구현 */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getNickname();
    }

    /** 계정 상태 */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.getDeletedAt() == null; }
}
