package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserService userService;

    public record MySessionDto(
            String sessionId,
            UserDetailResponseDto leftUser,
            UserDetailResponseDto rightUser
    ) {}

    @GetMapping("/mine")
    public ResponseEntity<MySessionDto> getMySession(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return sessionService.findByUserId(principalDetails.memberId)
                .map(s -> {
                    var leftUser = userService.getUserDetail(s.getLeftUserId());   // UserResponseDto
                    var rightUser = userService.getUserDetail(s.getRightUserId()); // UserResponseDto

                    return ResponseEntity.ok(new MySessionDto(
                            s.getSessionId(),
                            leftUser,
                            rightUser
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{sessionId}/snapshot")
    @ResponseBody
    public ResponseEntity<SnapshotResponseDto> getSnapshot(@PathVariable String sessionId,
                                                           @AuthenticationPrincipal PrincipalDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SessionObject session = sessionService.getSessionObject(sessionId);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 세션 없음
        }
        long uid = principal.getUid();
        if (session.getLeftUserId() != uid && session.getRightUserId() != uid) {
            log.info("Session Control : "+session.getSessionId() + " " + session.getLeftUserId() + " " + session.getRightUserId() + " " + uid);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 참가자 아님
        }
        var loop = session.getGameLoop();
        if (loop == null) {
            return ResponseEntity.status(HttpStatus.GONE).build(); // 게임 종료됨
        }

        SnapshotResponseDto snapshotResponseDto = loop.getLastSnapshot(uid);
        log.info(snapshotResponseDto.toString());
        return ResponseEntity.ok(snapshotResponseDto); // 프레임 캐시 리턴
    }
}
