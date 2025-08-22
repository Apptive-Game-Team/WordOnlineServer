package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionManager sessionManager;
    private final UserService userService;

    @GetMapping("/mine")
    public ResponseEntity<?> getMySession(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        UserResponseDto user = userService.getUser(principalDetails.userId);

        return sessionManager.findByUserId(user.id())
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(new SessionDto(
                        s.getSessionId(),
                        s.getLeftUserId(),
                        s.getRightUserId()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("NO_SESSION"));
    }

    @lombok.Value
    static class SessionDto {
        String sessionId;
        long leftUserId;
        long rightUserId;
    }

    @GetMapping("/{sessionId}/snapshot")
    @ResponseBody
    public ResponseEntity<SnapshotResponseDto> getSnapshot(@PathVariable String sessionId,
                                                           @AuthenticationPrincipal PrincipalDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SessionObject session = sessionManager.getSessionObject(sessionId);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 세션 없음
        }
        long uid = principal.getUid();
        if (session.getLeftUserId() != uid && session.getRightUserId() != uid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 참가자 아님
        }
        var loop = session.getGameLoop();
        if (loop == null) {
            return ResponseEntity.status(HttpStatus.GONE).build(); // 게임 종료됨
        }
        return ResponseEntity.ok(loop.getLastSnapshot()); // 프레임 캐시 리턴
    }
}
