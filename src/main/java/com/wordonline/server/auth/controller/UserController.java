package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.KakaoUser;
import com.wordonline.server.auth.domain.PrincipalDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/users")
@RestController
public class UserController {
    @GetMapping("/mine")
    public ResponseEntity<KakaoUser> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(principalDetails.user);
    }
}
