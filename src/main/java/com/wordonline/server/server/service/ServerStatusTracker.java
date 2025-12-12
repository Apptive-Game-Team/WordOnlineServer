package com.wordonline.server.server.service;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.session.service.SessionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerStatusTracker {

    private final ConfigurableApplicationContext context;
    private final ServerStatusService serverStatusService;
    private final SessionService sessionService;

    private final Subscriber<Integer> onSessionNumChange = new Subscriber<>() {
        private Subscription subscription;
        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1); // 구독 시작
        }

        @Override
        public void onNext(Integer item) {
            subscription.request(1);
            if (item == 0) {
                onSessionZero();
            }
            System.out.println("Session number changed: " + item);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Completed");
        }
    };

    private void onSessionZero() {
        if (serverStatusService.getCurrentState().equals(ServerState.DRAINING)) {
            SpringApplication.exit(context, () -> 0);
            System.exit(0);
        }
    }

    @PostConstruct
    public void onStart() {
        serverStatusService.setServerStatus(ServerState.ACTIVE);
        sessionService.subscribeSessionNumChange(onSessionNumChange);
    }

    @PreDestroy
    public void onDestroy() {
        serverStatusService.setServerStatus(ServerState.INACTIVE);
    }
}
