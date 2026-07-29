package com.wordonline.server.session.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionServiceTest {

    @Test
    void reportsInactiveForUnknownSessionId() {
        SessionService service = new SessionService(null, null, null, null, null);

        assertThat(service.isSessionActive("no-such-session")).isFalse();
    }
}
