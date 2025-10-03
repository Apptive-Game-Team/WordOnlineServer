package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/mine")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(userService.getUser(principalDetails.memberId));
    }

    @DeleteMapping("/mine")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (userService.deleteUser(principalDetails.memberId)) {
            return ResponseEntity.ok("successfully delete");
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/mine/status")
    public ResponseEntity<Map<String, String>> getMyStatus(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        var status = userService.getStatus(principalDetails.memberId);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }
}
