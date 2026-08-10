package com.wordonline.server.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Identity and capacity settings this game server publishes to the shared
 * {@code servers} table so the lobby can route players to it.
 */
@ConfigurationProperties(prefix = "server")
public record ServerIdentityProperties(
        String protocol,
        String domain,
        Integer externalPort,
        Integer maxSessions) {
}
