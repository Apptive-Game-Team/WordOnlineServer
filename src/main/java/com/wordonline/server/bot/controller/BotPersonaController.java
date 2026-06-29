package com.wordonline.server.bot.controller;

import com.wordonline.server.bot.dto.BotPersonaRequestDto;
import com.wordonline.server.bot.dto.BotPersonaResponseDto;
import com.wordonline.server.bot.service.BotPersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@RestController
@RequestMapping("/api/admin/bots")
@RequiredArgsConstructor
public class BotPersonaController {

    private final BotPersonaService botPersonaService;

    @GetMapping
    public ResponseEntity<List<BotPersonaResponseDto>> findAll() {
        return ResponseEntity.ok(botPersonaService.findAll().stream()
                .map(BotPersonaResponseDto::new)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BotPersonaResponseDto> findById(@PathVariable long id) {
        return botPersonaService.findById(id)
                .map(BotPersonaResponseDto::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BotPersonaResponseDto> create(@RequestBody BotPersonaRequestDto requestDto) {
        return ResponseEntity.ok(new BotPersonaResponseDto(botPersonaService.create(requestDto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BotPersonaResponseDto> update(@PathVariable long id,
                                                        @RequestBody BotPersonaRequestDto requestDto) {
        return ResponseEntity.ok(new BotPersonaResponseDto(botPersonaService.update(id, requestDto)));
    }
}
