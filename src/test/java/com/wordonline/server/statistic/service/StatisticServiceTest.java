package com.wordonline.server.statistic.service;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticServiceTest {

    @Test
    void saveGameResultIsTransactionalSoStatisticInsertsCommitTogether() throws Exception {
        Transactional annotation = StatisticService.class
                .getDeclaredMethod("saveGameResult", GameContext.class, Master.class, SessionType.class)
                .getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
    }
}
