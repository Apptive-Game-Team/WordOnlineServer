package com.wordonline.server.auth.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PrincipalDetails implements UserDetails, OAuth2User {

    public final KakaoUser user;
    private Map<String, Object> attributes;

    public PrincipalDetails(KakaoUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(
                new GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return "ROLE_user";
                    }
                }
        );
        return authorities;
    }

    public Long getUid() {
        return user.id();
    }

    public String getEmail() {
        return user.email();
    }

    public String getImageUrl() {
        return user.imageUrl();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.nickname();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.nickname();
    }
}
