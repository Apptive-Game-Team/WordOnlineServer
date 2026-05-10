package com.wordonline.server.game.service;

import com.wordonline.server.game.repository.ParameterRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;
    private final ParameterProfileContext parameterProfileContext;

    protected ParameterService(ParameterRepository parameterRepository,
                               ParameterProfileContext parameterProfileContext) {
        this.parameterRepository = parameterRepository;
        this.parameterProfileContext = parameterProfileContext;
    }

    private final Map<ParameterCacheKey, Double> parameterCaches = new ConcurrentHashMap<>();

    public void invalidateCache() {
        parameterCaches.clear();
    }

    public double getValue(String gameObject, String parameterName) {
        Long parameterProfileId = parameterProfileContext.getCurrentProfileId();
        ParameterCacheKey cacheKey = new ParameterCacheKey(gameObject, parameterName, parameterProfileId);

        Double cachedValue = parameterCaches.get(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        Double valueFromDb = parameterRepository.getParameterValue(gameObject, parameterName, parameterProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + gameObject + ", " + parameterName));
        parameterCaches.put(cacheKey, valueFromDb);
        return valueFromDb;
    }

    private record ParameterCacheKey(String gameObject, String parameterName, Long parameterProfileId) {
    }
}
