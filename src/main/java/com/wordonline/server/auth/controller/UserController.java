package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.AuthResponseDto;
import com.wordonline.server.auth.dto.UserLoginRequestDto;
import com.wordonline.server.auth.dto.UserRegisterRequestDto;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.AuthService;
import com.wordonline.server.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RequestMapping("/api/users")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<AuthResponseDto> registerAndLogin(
            @RequestBody UserRegisterRequestDto userRegisterRequestDto
            ) throws AuthenticationException {
        return ResponseEntity.ok(authService.registerAndLogin(userRegisterRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody UserLoginRequestDto userLoginRequestDto
    ) throws AuthenticationException {
        return ResponseEntity.ok(authService.login(userLoginRequestDto));
    }


    @GetMapping("/mine")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request) throws AuthenticationException {
        if (principalDetails == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("tlqkf");

        return ResponseEntity.ok(userService.getUser(principalDetails.userId));
    }
}
