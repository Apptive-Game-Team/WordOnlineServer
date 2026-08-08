package com.wordonline.server.game.service;

import com.wordonline.server.game.repository.ParameterRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;

    protected ParameterService(ParameterRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    private Map<String, Map<String, Double>> parameterCaches = new ConcurrentHashMap<>();

    public void invalidateCache() {
        parameterCaches.clear();
    }

    public double getValue(String gameObject, String parameterName) {
        return findValue(gameObject, parameterName)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + gameObject + ", " + parameterName));
    }

    public double getValueOrDefault(String gameObject, String parameterName, double defaultValue) {
        return findValue(gameObject, parameterName).orElse(defaultValue);
    }

    private java.util.Optional<Double> findValue(String gameObject, String parameterName) {
        Map<String, Double> objectParameters = parameterCaches.get(gameObject);
        if (objectParameters != null && objectParameters.containsKey(parameterName)) {
            return java.util.Optional.of(objectParameters.get(parameterName));
        }

        var valueFromDb = parameterRepository.getParameterValue(gameObject, parameterName);
        valueFromDb.ifPresent(value -> parameterCaches
                .computeIfAbsent(gameObject, k -> new ConcurrentHashMap<>())
                .put(parameterName, value));
        return valueFromDb;
    }
}
