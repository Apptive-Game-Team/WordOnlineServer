package com.wordonline.server.game.repository;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveScenarioEvent;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.dto.Master;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PveScenarioRepository {

    private static final String FIND_OBJECTIVES = """
            SELECT installer_id
            FROM pve_scenario_objectives
            WHERE scenario_id = :scenarioId
            ORDER BY sort_order
            """;

    private static final String FIND_INSTALLERS = """
            SELECT installer_id, prefab_type, master, position_x, position_y, position_z
            FROM pve_scenario_installers
            WHERE scenario_id = :scenarioId
            ORDER BY sort_order
            """;

    private static final String FIND_EVENTS = """
            SELECT id, event_id, trigger_type, trigger_value, speaker_installer_id, message_key
            FROM pve_scenario_events
            WHERE scenario_id = :scenarioId
            ORDER BY sort_order
            """;

    private static final String FIND_EVENT_LINES = """
            SELECT line_text
            FROM pve_scenario_event_lines
            WHERE event_row_id = :eventRowId
            ORDER BY line_order
            """;

    private final JdbcClient jdbcClient;

    public Optional<PveScenario> findById(Long scenarioId) {
        return Optional.of(new PveScenario(
                        findObjectives(scenarioId),
                        findInstallers(scenarioId),
                        findEvents(scenarioId)
                ));
    }

    private List<String> findObjectives(Long scenarioId) {
        return jdbcClient.sql(FIND_OBJECTIVES)
                .param("scenarioId", scenarioId)
                .query(String.class)
                .list();
    }

    private List<PveInstallObject> findInstallers(Long scenarioId) {
        return jdbcClient.sql(FIND_INSTALLERS)
                .param("scenarioId", scenarioId)
                .query((rs, rowNum) -> new PveInstallObject(
                        rs.getString("installer_id"),
                        PrefabType.valueOf(rs.getString("prefab_type")),
                        Master.valueOf(rs.getString("master")),
                        new Vector3(
                                rs.getInt("position_x"),
                                rs.getInt("position_y"),
                                rs.getInt("position_z")
                        )
                ))
                .list();
    }

    private List<PveScenarioEvent> findEvents(Long scenarioId) {
        List<EventRow> eventRows = jdbcClient.sql(FIND_EVENTS)
                .param("scenarioId", scenarioId)
                .query((rs, rowNum) -> new EventRow(
                        rs.getLong("id"),
                        rs.getString("event_id"),
                        rs.getString("trigger_type"),
                        rs.getInt("trigger_value"),
                        rs.getString("speaker_installer_id"),
                        rs.getString("message_key")
                ))
                .list();

        return eventRows.stream()
                .map(row -> new PveScenarioEvent(
                        row.eventId(),
                        PveTriggerType.valueOf(row.triggerType()),
                        row.triggerValue(),
                        row.speakerInstallerId(),
                        row.messageKey(),
                        findEventLines(row.id())
                ))
                .toList();
    }

    private List<String> findEventLines(long eventRowId) {
        return jdbcClient.sql(FIND_EVENT_LINES)
                .param("eventRowId", eventRowId)
                .query(String.class)
                .list();
    }

    private record EventRow(
            long id,
            String eventId,
            String triggerType,
            int triggerValue,
            String speakerInstallerId,
            String messageKey
    ) {
    }
}
