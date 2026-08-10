package com.wordonline.server.server.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Boot generation of this process.
 *
 * <p>Sessions live only in {@code SessionService}'s in-memory map, so a restart drops every
 * one of them without telling anybody. The restarted process comes back on the same domain
 * and port and passes health checks, which leaves the lobby unable to tell "briefly
 * unreachable" from "restarted and lost every session" - its match tickets stay stuck in
 * MATCHED. A value generated once per process closes that gap: the lobby stores the id it
 * saw when the session was created and compares it against the id this process publishes,
 * and a mismatch is proof the session is gone.
 *
 * <p>Deliberately not a configuration property. A configured value would survive a restart
 * of the same deployment, which is exactly the case this has to detect.
 */
@Service
public class ServerInstanceIdProvider {

    private final String instanceId = UUID.randomUUID().toString();

    /**
     * @return the id of the currently running process, constant for its whole lifetime.
     */
    public String getInstanceId() {
        return instanceId;
    }
}
