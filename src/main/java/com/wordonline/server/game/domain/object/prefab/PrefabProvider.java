package com.wordonline.server.game.domain.object.prefab;

import org.springframework.beans.BeansException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
public class PrefabProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static PrefabInitializer get(PrefabType prefabType) {
        return applicationContext.getBean(prefabType.getBeanName(), PrefabInitializer.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        PrefabProvider.applicationContext = applicationContext;
    }
}
