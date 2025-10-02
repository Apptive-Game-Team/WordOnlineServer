package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.AuthResponseDto;
import com.wordonline.server.auth.dto.UserLoginRequestDto;
import com.wordonline.server.auth.dto.UserRegisterRequestDto;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.AuthService;
import com.wordonline.server.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.LinkedHashMap;
import java.util.Map;

@RequestMapping("/api/users")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<AuthResponseDto> registerAndLogin(
            @Validated @RequestBody UserRegisterRequestDto userRegisterRequestDto
            ) {
        log.info("[Login] User register; userEmail: {}", userRegisterRequestDto.email());
        return ResponseEntity.ok(authService.registerAndLogin(userRegisterRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Validated @RequestBody UserLoginRequestDto userLoginRequestDto
    ) {
        AuthResponseDto authResponseDto = authService.login(userLoginRequestDto);
        log.info("[Login] User login; userEmail: {}", userLoginRequestDto.email());
        return ResponseEntity.ok(authResponseDto);
    }

    @GetMapping("/mine")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.getUser(principalDetails.userId));
    }

    @DeleteMapping("/mine")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (userService.deleteUser(principalDetails.userId)) {
            return ResponseEntity.ok("successfully delete");
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/mine/status")
    public ResponseEntity<Map<String, String>> getMyStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var status = userService.getStatus(principalDetails.userId);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }
}
