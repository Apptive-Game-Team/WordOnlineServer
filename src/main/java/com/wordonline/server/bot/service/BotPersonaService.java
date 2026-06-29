package com.wordonline.server.bot.service;

import com.wordonline.server.bot.domain.BotParticipant;
import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.dto.BotPersonaRequestDto;
import com.wordonline.server.bot.repository.BotPersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BotPersonaService {

    private final BotPersonaRepository botPersonaRepository;

    @Transactional(readOnly = true)
    public List<BotPersona> findAll() {
        return botPersonaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BotPersona> findEnabled() {
        return findAll().stream()
                .filter(BotPersona::enabled)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BotPersona> findById(long id) {
        return botPersonaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public BotPersona findOrDefault(long id) {
        return findById(id).orElse(BotPersona.DEFAULT);
    }

    @Transactional(readOnly = true)
    public BotPersona findByParticipantIdOrDefault(long participantId) {
        if (!BotParticipant.isBot(participantId)) {
            return BotPersona.DEFAULT;
        }
        return findOrDefault(BotParticipant.personaId(participantId));
    }

    public BotPersona create(BotPersonaRequestDto requestDto) {
        return botPersonaRepository.create(normalize(requestDto));
    }

    public BotPersona update(long id, BotPersonaRequestDto requestDto) {
        botPersonaRepository.update(id, normalize(requestDto));
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Bot persona not found: " + id));
    }

    public void setMmr(long id, short mmr) {
        botPersonaRepository.setMmr(id, mmr);
    }

    private BotPersonaRequestDto normalize(BotPersonaRequestDto requestDto) {
        return new BotPersonaRequestDto(
                requestDto.name(),
                requestDto.tier(),
                requestDto.deckId(),
                Math.max(0, requestDto.thinkingTimeMs()),
                Math.max(1, requestDto.reactionIntervalFrames()),
                Math.max(0.0, Math.min(1.0, requestDto.counterAggression())),
                requestDto.mmr(),
                requestDto.enabled()
        );
    }
}
