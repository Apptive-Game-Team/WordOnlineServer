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

    public double getValue(String gameObject, String parameterName) {

        Map<String, Double> objectParameters = parameterCaches.get(gameObject);
        if (objectParameters != null && objectParameters.containsKey(parameterName)) {
            return objectParameters.get(parameterName);
        }

        Double valueFromDb = parameterRepository.getParameterValue(gameObject, parameterName);
        if (valueFromDb != null) {
            parameterCaches
                    .computeIfAbsent(gameObject, k -> new ConcurrentHashMap<>())
                    .put(parameterName, valueFromDb);
            return valueFromDb;
        }

        throw new IllegalArgumentException("Parameter not found: " + gameObject + ", " + parameterName);
    }
}
