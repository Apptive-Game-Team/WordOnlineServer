package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.domain.KakaoUser;
import com.wordonline.server.auth.domain.PrincipalDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RequestMapping("/api/users")
@RestController
public class UserController {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    @GetMapping("/mine")
    public ResponseEntity<KakaoUser> getUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {

            // TODO - remove mock data
            return ResponseEntity.ok(new KakaoUser(
                    atomicInteger.getAndIncrement(),
                    "mock email",
                    "demo-user",
                    "mock user"
            ));

//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(principalDetails.user);
    }
}
