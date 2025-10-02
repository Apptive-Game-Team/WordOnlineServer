package com.wordonline.server.auth.config;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final UserRepository userRepository;

    public Authentication getAuthentication(String token) {
        Jwt jwt = jwtDecoder.decode(token);

        String auths = jwt.getClaim("scope");

        Long memberId = jwt.getClaim("memberId");

        Collection<GrantedAuthority> authorities = Arrays.stream(auths.split(" "))
                .map(value -> (GrantedAuthority) () -> value)
                .collect(Collectors.toList());

        PrincipalDetails principal = new PrincipalDetails(memberId);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
