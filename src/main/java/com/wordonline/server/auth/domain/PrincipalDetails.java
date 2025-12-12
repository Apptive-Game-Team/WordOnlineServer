package com.wordonline.server.auth.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

public class PrincipalDetails implements UserDetails, OAuth2User, Principal {

    public final Long memberId;
    private Map<String, Object> attributes;
    private final Collection<GrantedAuthority> authorities;

    public PrincipalDetails(Long memberId, Collection<GrantedAuthority> authorities) {
        this.memberId = memberId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getUid() {
        return memberId;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "user" + memberId;
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
        return getUsername();
    }
}
