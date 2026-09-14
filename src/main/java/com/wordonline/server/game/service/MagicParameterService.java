package com.wordonline.server.game.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.repository.MagicParameterRepository;

/**
 * 마법별 시전 값을 읽어 둔다. {@link ParameterService} 가 {@code parameter_values} 를 읽는 것과
 * 같은 방식으로, hit 과 miss 를 모두 캐시한다 — 값이 없는 것은 정상이고(계열 기본값을 쓴다),
 * miss 를 캐시하지 않으면 같은 조회가 매번 JDBC 를 다시 친다.
 */
@Service
public class MagicParameterService {

    private final MagicParameterRepository magicParameterRepository;

    private final Map<Long, Map<String, Optional<Double>>> parameterCaches = new ConcurrentHashMap<>();

    public MagicParameterService(MagicParameterRepository magicParameterRepository) {
        this.magicParameterRepository = magicParameterRepository;
    }

    public void invalidateCache() {
        parameterCaches.clear();
    }

    public Optional<Double> findValue(long magicId, ParameterKey parameterKey) {
        String parameterName = parameterKey.dbName();

        Map<String, Optional<Double>> magicParameters = parameterCaches.get(magicId);
        if (magicParameters != null) {
            Optional<Double> cached = magicParameters.get(parameterName);
            if (cached != null) {
                return cached;
            }
        }

        Optional<Double> valueFromDb = magicParameterRepository.getParameterValue(magicId, parameterName);
        parameterCaches
                .computeIfAbsent(magicId, key -> new ConcurrentHashMap<>())
                .put(parameterName, valueFromDb);
        return valueFromDb;
    }

    public double getValueOrDefault(long magicId, ParameterKey parameterKey, double defaultValue) {
        return findValue(magicId, parameterKey).orElse(defaultValue);
    }
}
