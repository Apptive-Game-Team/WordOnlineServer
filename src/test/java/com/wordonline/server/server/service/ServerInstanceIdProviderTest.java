package com.wordonline.server.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ServerInstanceIdProviderTest {

    @Test
    void 같은_프로세스_안에서는_값이_변하지_않는다() {
        ServerInstanceIdProvider provider = new ServerInstanceIdProvider();

        assertThat(provider.getInstanceId()).isEqualTo(provider.getInstanceId());
    }

    @Test
    void 새로_기동된_인스턴스는_다른_값을_가진다() {
        // Stands in for a restart: same configuration, new process, and the lobby has to be
        // able to tell them apart. A configured or derived value would collide here.
        assertThat(new ServerInstanceIdProvider().getInstanceId())
                .isNotEqualTo(new ServerInstanceIdProvider().getInstanceId());
    }

    @Test
    void 값은_UUID_문자열이다() {
        String instanceId = new ServerInstanceIdProvider().getInstanceId();

        assertThatCode(() -> UUID.fromString(instanceId)).doesNotThrowAnyException();
        // The servers.instance_id column is VARCHAR(64).
        assertThat(instanceId).hasSizeLessThanOrEqualTo(64);
    }
}
