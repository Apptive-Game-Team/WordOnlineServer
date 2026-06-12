package com.wordonline.server.bot.repository;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.bot.dto.BotPersonaRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BotPersonaRepository {

    private static final String FIND_ALL = """
            SELECT id, name, tier, deck_id, thinking_time_ms, reaction_interval_frames,
                   counter_aggression, mmr, enabled
            FROM bot_personas
            ORDER BY id;
            """;

    private static final String FIND_BY_ID = """
            SELECT id, name, tier, deck_id, thinking_time_ms, reaction_interval_frames,
                   counter_aggression, mmr, enabled
            FROM bot_personas
            WHERE id = :id;
            """;

    private static final String INSERT = """
            INSERT INTO bot_personas(name, tier, deck_id, thinking_time_ms, reaction_interval_frames,
                                     counter_aggression, mmr, enabled)
            VALUES(:name, :tier::bot_tier, :deckId, :thinkingTimeMs, :reactionIntervalFrames,
                   :counterAggression, :mmr, :enabled)
            RETURNING id;
            """;

    private static final String UPDATE = """
            UPDATE bot_personas
            SET name = :name,
                tier = :tier::bot_tier,
                deck_id = :deckId,
                thinking_time_ms = :thinkingTimeMs,
                reaction_interval_frames = :reactionIntervalFrames,
                counter_aggression = :counterAggression,
                mmr = :mmr,
                enabled = :enabled
            WHERE id = :id;
            """;

    private static final String SET_MMR = """
            UPDATE bot_personas
            SET mmr = :mmr
            WHERE id = :id;
            """;

    private final JdbcClient jdbcClient;

    public List<BotPersona> findAll() {
        return jdbcClient.sql(FIND_ALL)
                .query(this::map)
                .list();
    }

    public Optional<BotPersona> findById(long id) {
        return jdbcClient.sql(FIND_BY_ID)
                .param("id", id)
                .query(this::map)
                .optional();
    }

    public BotPersona create(BotPersonaRequestDto requestDto) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        bind(jdbcClient.sql(INSERT), requestDto)
                .update(keyHolder);
        return findById(keyHolder.getKey().longValue())
                .orElseThrow(() -> new IllegalStateException("Created bot persona not found."));
    }

    public void update(long id, BotPersonaRequestDto requestDto) {
        bind(jdbcClient.sql(UPDATE).param("id", id), requestDto)
                .update();
    }

    public void setMmr(long id, short mmr) {
        jdbcClient.sql(SET_MMR)
                .param("id", id)
                .param("mmr", mmr)
                .update();
    }

    private JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, BotPersonaRequestDto requestDto) {
        return spec
                .param("name", requestDto.name())
                .param("tier", requestDto.tier().name())
                .param("deckId", requestDto.deckId())
                .param("thinkingTimeMs", requestDto.thinkingTimeMs())
                .param("reactionIntervalFrames", requestDto.reactionIntervalFrames())
                .param("counterAggression", requestDto.counterAggression())
                .param("mmr", requestDto.mmr())
                .param("enabled", requestDto.enabled() == null || requestDto.enabled());
    }

    private BotPersona map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new BotPersona(
                rs.getLong("id"),
                rs.getString("name"),
                BotTier.valueOf(rs.getString("tier")),
                rs.getLong("deck_id"),
                rs.getInt("thinking_time_ms"),
                rs.getInt("reaction_interval_frames"),
                rs.getDouble("counter_aggression"),
                rs.getShort("mmr"),
                rs.getBoolean("enabled")
        );
    }
}
