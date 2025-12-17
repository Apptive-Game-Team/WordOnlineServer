package com.wordonline.server.game.controller;

import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.service.ParameterService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ParameterController {

    private final ParameterService parameterService;
    private final DatabaseMagicParser databaseMagicParser;

    @PreAuthorize("hasAuthority('WORDONLINE_SERVER')")
    @PostMapping("/invalidate")
    public void invalidateParameterCache() {
        parameterService.invalidateCache();
        databaseMagicParser.invalidateCache();
    }
}
