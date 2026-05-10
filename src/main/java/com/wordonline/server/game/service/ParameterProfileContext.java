package com.wordonline.server.game.service;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class ParameterProfileContext {

    private final ThreadLocal<Long> currentProfileId = new ThreadLocal<>();

    public Long getCurrentProfileId() {
        return currentProfileId.get();
    }

    public void runWithProfile(Long parameterProfileId, Runnable action) {
        Long previous = currentProfileId.get();
        currentProfileId.set(parameterProfileId);
        try {
            action.run();
        } finally {
            restore(previous);
        }
    }

    public <T> T callWithProfile(Long parameterProfileId, Supplier<T> action) {
        Long previous = currentProfileId.get();
        currentProfileId.set(parameterProfileId);
        try {
            return action.get();
        } finally {
            restore(previous);
        }
    }

    private void restore(Long previous) {
        if (previous == null) {
            currentProfileId.remove();
            return;
        }
        currentProfileId.set(previous);
    }
}
