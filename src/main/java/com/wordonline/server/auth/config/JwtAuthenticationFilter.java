package com.wordonline.server.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token = resolveToken((HttpServletRequest) servletRequest);

        if (token != null) {
            try {
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Not Valid Token
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                log.error("Jwt Provier", e);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null) {
            return null;
        }

        return token.replace(JwtProvider.JWT_PREFIX, "");
    }
}
