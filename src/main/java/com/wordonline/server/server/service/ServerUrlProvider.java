package com.wordonline.server.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServerUrlProvider {

    @Value("${server.external-port}")
    private Integer port;

    @Value("${server.domain}")
    private String domain;

    @Value("${server.protocol}")
    private String protocol;

    public String getServerUrl() {
        return String.format("%s://%s:%d", protocol, domain, port);
    }
}
