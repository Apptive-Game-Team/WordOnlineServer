package com.wordonline.server.auth.config;

import com.wordonline.server.auth.domain.PrincipalDetails;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProvider {

    public static final String JWT_PREFIX = "Bearer ";
    private final JwtDecoder jwtDecoder;

    public Authentication getAuthentication(String token) {
        Jwt jwt = jwtDecoder.decode(token);

        String auths = jwt.getClaim("scope");

        Long userId = jwt.getClaim("memberId");

        Collection<GrantedAuthority> authorities = Arrays.stream(auths.split(" "))
                .map(value -> (GrantedAuthority) () -> value)
                .collect(Collectors.toList());

        PrincipalDetails principal = new PrincipalDetails(userId);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
