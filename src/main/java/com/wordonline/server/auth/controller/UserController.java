package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.KakaoUser;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;
import java.util.concurrent.atomic.AtomicInteger;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static AtomicInteger atomicInteger = new AtomicInteger(123);

    @GetMapping("/mine")
    public ResponseEntity<KakaoUser> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request) throws AuthenticationException {
//        if (principalDetails == null) {

            KakaoUser kakaoUser = new KakaoUser(
                    atomicInteger.getAndIncrement(),
                    "mock email " + atomicInteger.get(),
                    "demo-user-" + atomicInteger.get(),
                    "mock user " + atomicInteger.get()
            );

            userService.loginOrRegisterUser(kakaoUser);

            PrincipalDetails principal = new PrincipalDetails(kakaoUser);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());


            // TODO - remove mock data
            return ResponseEntity.status(HttpStatus.CREATED).body(kakaoUser);

//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }


//        return ResponseEntity.ok(principalDetails.user);
    }
}
