package com.wordonline.server.game.service;

import com.wordonline.server.data.service.GameDataService;
import com.wordonline.server.game.repository.ParameterRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;
    private final GameDataService gameDataService;

    protected ParameterService(ParameterRepository parameterRepository, @Lazy GameDataService gameDataService) {
        this.parameterRepository = parameterRepository;
        this.gameDataService = gameDataService;
    }

    private Map<String, Map<String, Double>> parameterCaches = new ConcurrentHashMap<>();

    public void invalidateCache() {
        parameterCaches.clear();
        gameDataService.invalidate();
    }

    public double getValue(String gameObject, String parameterName) {

        Map<String, Double> objectParameters = parameterCaches.get(gameObject);
        if (objectParameters != null && objectParameters.containsKey(parameterName)) {
            return objectParameters.get(parameterName);
        }


        Double valueFromDb = parameterRepository.getParameterValue(gameObject, parameterName)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + gameObject + ", " + parameterName));


        parameterCaches
                .computeIfAbsent(gameObject, k -> new ConcurrentHashMap<>())
                .put(parameterName, valueFromDb);
        return valueFromDb;
    }
}
