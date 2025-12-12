package com.wordonline.server.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import com.wordonline.server.session.service.SessionService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/server/servers")
@Controller
@RequiredArgsConstructor
public class ServerStateController {

    private final ServerStatusService serverStatusService;
    private final SessionService sessionService;

    @PreAuthorize("hasAuthority('WORDONLINE_CICD')")
    @PostMapping("/mine/state/draining")
    public void setStateDraining() {
        serverStatusService.setServerStatus(ServerState.DRAINING);
        sessionService.submitSessionNumChange();
    }
}

