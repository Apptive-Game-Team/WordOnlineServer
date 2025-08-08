package com.wordonline.server.game.controller;

import com.wordonline.server.game.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ParameterController {

    private final ParameterService parameterService;

    @GetMapping("/invalidate")
    public void invalidateParameterCache() {
        parameterService.invalidateCache();
    }
}
