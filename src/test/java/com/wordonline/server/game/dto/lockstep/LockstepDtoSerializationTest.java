package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LockstepDtoSerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesFixedMessageTypesWithProtocolValues() throws Exception {
        JsonNode confirmedFrame = objectMapper.readTree(objectMapper.writeValueAsString(
                new ConfirmedFrameDto(1, 2, List.of(), Map.of(), true)));
        JsonNode abort = objectMapper.readTree(objectMapper.writeValueAsString(
                new LockstepAbortDto(2, LockstepAbortReason.PEER_HASH_MISMATCH, Set.of(10L))));

        assertThat(confirmedFrame.get("type").asText()).isEqualTo("confirmedFrame");
        assertThat(abort.get("type").asText()).isEqualTo("lockstepAbort");
        assertThat(abort.get("reason").asText()).isEqualTo("peer-hash-mismatch");
    }

    @Test
    void deserializesKnownInputType() throws Exception {
        FrameInputDto input = objectMapper.readValue("""
                {"sequence":0,"type":"useMagic","id":1,"cards":[],"position":null}
                """, FrameInputDto.class);

        assertThat(input.type()).isEqualTo(LockstepInputType.USE_MAGIC);
    }
}
