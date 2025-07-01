package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.KakaoUser;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.KakaoUserInfoResponseDto;
import com.wordonline.server.auth.service.KakaoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @ResponseBody
    @GetMapping("")
    public ResponseEntity<String> loginKakao() {
        return new ResponseEntity<>(
                kakaoAuthService.getLoginRedirectHeader(),
                HttpStatus.FOUND);
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpServletRequest request) {
        String accessToken = kakaoAuthService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfoResponseDto = kakaoAuthService.getUserInfo(accessToken);
        KakaoUser kakaoUser = KakaoUser.from(userInfoResponseDto);
        PrincipalDetails principal = new PrincipalDetails(kakaoUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        HttpSession session = request.getSession(true);
//        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return "kakao-login-complete";
    }
}
