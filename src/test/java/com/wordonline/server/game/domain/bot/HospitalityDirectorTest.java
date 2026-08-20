package com.wordonline.server.game.domain.bot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HospitalityDirectorTest {

    private final HospitalityDirector director = new HospitalityDirector();

    // 플레이어 보드의 일정 비율까지만 쓴다. 한도를 끝까지 채우면 늘 아슬아슬하게만 약한
    // 상대가 되어, 접대가 아니라 빡빡한 라이벌이 된다.
    @Test
    void leavesTheBotWellUnderThePlayersBoard() {
        assertThat(director.summonAllowance(100, 0)).isEqualTo(70);
    }

    @Test
    void countsWhatTheBotAlreadyHasOutAgainstItsAllowance() {
        assertThat(director.summonAllowance(100, 50)).isEqualTo(20);
    }

    // 이미 비율을 넘겨 두었으면 더 낼 것이 없다.
    @Test
    void allowsNothingOnceTheBotHasSpentItsShare() {
        assertThat(director.summonAllowance(100, 70)).isNotPositive();
    }

    @Test
    void allowsNothingAgainstAnEmptyBoard() {
        assertThat(director.summonAllowance(0, 0)).isNotPositive();
    }

    @Test
    void handsTheFightOverWhenThePlayerIsNotAheadOnBoard() {
        assertThat(director.aggression(0, 0, 0.0)).isEqualTo(HospitalityDirector.FULL);
        assertThat(director.aggression(1, 3, 0.0)).isEqualTo(HospitalityDirector.FULL);
    }

    @Test
    void easesOffOnceThePlayerIsClearlyAhead() {
        assertThat(director.aggression(3, 1, 0.0)).isEqualTo(HospitalityDirector.EASED);
    }

    // Unit counts miss a player who is winning with one strong body rather than several.
    @Test
    void easesOffWhenThePlayerIsPushingHardRegardlessOfCounts() {
        assertThat(director.aggression(1, 0, 0.9)).isEqualTo(HospitalityDirector.EASED);
    }

    @Test
    void staysBetweenTheTwoOnACloseBoard() {
        assertThat(director.aggression(1, 0, 0.0)).isEqualTo(HospitalityDirector.PARTIAL);
    }

    // The whole range is negative on purpose. A non-negative value would make the tutorial
    // opponent start playing to win, which is the one thing it must never do.
    @Test
    void neverAsksForAPlayThatBeatsThePlayer() {
        for (int playerUnits = 0; playerUnits <= 5; playerUnits++) {
            for (int botUnits = 0; botUnits <= 5; botUnits++) {
                for (double pressure = 0.0; pressure <= 1.0; pressure += 0.25) {
                    assertThat(director.aggression(playerUnits, botUnits, pressure))
                            .isLessThan(0.0)
                            .isGreaterThanOrEqualTo(HospitalityDirector.FULL);
                }
            }
        }
    }
}
