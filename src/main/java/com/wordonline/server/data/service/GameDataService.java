package com.wordonline.server.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordonline.server.data.dto.*;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.magic.ElementalChart;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameDataService {

    private final DatabaseMagicParser magicParser;
    private final ParameterRepository parameterRepository;
    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    private volatile String cachedVersion = null;
    private volatile GameConfigDto cachedConfig = null;

    public String getVersion() {
        if (cachedVersion == null) buildCache();
        return cachedVersion;
    }

    public GameConfigDto getConfig() {
        if (cachedConfig == null) buildCache();
        return cachedConfig;
    }

    public void invalidate() {
        cachedVersion = null;
        cachedConfig = null;
        log.info("[GameDataService] Cache invalidated");
    }

    private synchronized void buildCache() {
        if (cachedConfig != null) return; // double-checked locking

        GameConfigDto config = aggregate();

        try {
            // Temporarily set version to empty to produce stable JSON for hashing
            config.setVersion("");
            String json = objectMapper.writeValueAsString(config);
            String hash = sha256Hex(json);
            config.setVersion(hash);
            cachedConfig = config;
            cachedVersion = hash;
            log.info("[GameDataService] Cache built, version={}", hash);
        } catch (Exception e) {
            log.error("[GameDataService] Failed to build cache", e);
        }
    }

    private GameConfigDto aggregate() {
        return new GameConfigDto(
                null,
                buildMagicRecipes(),
                parameterRepository.getAllParameterValues(),
                buildElementalChart(),
                buildCardDefinitions()
        );
    }

    private List<MagicRecipeDto> buildMagicRecipes() {
        Map<List<CardType>, Magic> recipeMap = magicParser.getAllMagicRecipeMap();
        List<MagicRecipeDto> result = new ArrayList<>(recipeMap.size());
        for (Map.Entry<List<CardType>, Magic> entry : recipeMap.entrySet()) {
            Magic magic = entry.getValue();
            List<String> cardNames = entry.getKey().stream()
                    .map(CardType::name)
                    .toList();
            result.add(new MagicRecipeDto(magic.id, magic.getClass().getSimpleName(), cardNames));
        }
        result.sort(Comparator.comparingLong(MagicRecipeDto::id));
        return result;
    }

    private List<ElementalChartEntryDto> buildElementalChart() {
        ElementType[] types = ElementType.values();
        List<ElementalChartEntryDto> result = new ArrayList<>();
        for (ElementType atk : types) {
            for (ElementType def : types) {
                float multiplier = ElementalChart.getMultiplier(atk, def);
                if (multiplier != 1.0f) {
                    result.add(new ElementalChartEntryDto(atk.name(), def.name(), multiplier));
                }
            }
        }
        return result;
    }

    private List<CardDefinitionDto> buildCardDefinitions() {
        return jdbcClient.sql("SELECT id, name, card_type FROM cards ORDER BY id")
                .query((rs, rowNum) -> new CardDefinitionDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("card_type")
                ))
                .list();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
