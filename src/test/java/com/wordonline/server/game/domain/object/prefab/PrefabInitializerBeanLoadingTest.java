package com.wordonline.server.game.domain.object.prefab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;

/**
 * Calling a constructor directly proves nothing about whether Spring can pick it.
 * A prefab initializer with two constructors and no {@code @Autowired} compiles,
 * passes its own unit test, and then fails at bean creation time on a running
 * server, so this test lets Spring create every prefab bean instead.
 */
class PrefabInitializerBeanLoadingTest {

    private static final String INITIALIZER_PACKAGE =
            "com.wordonline.server.game.domain.object.prefab.implement";

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void createContext() {
        context = new AnnotationConfigApplicationContext();
        // The initializers read these only while initializing a game object, never while
        // being constructed, so stubs are enough to let Spring wire and create them all.
        // registerSingleton, not registerBean: it hands Spring a finished object, so the
        // stub's @PostConstruct is skipped along with the database it would have read.
        context.getBeanFactory().registerSingleton("parameters", mock(Parameters.class));
        context.getBeanFactory().registerSingleton("databaseMagicParser", mock(DatabaseMagicParser.class));
        context.scan(INITIALIZER_PACKAGE);
        context.refresh();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void springCreatesAnInitializerBeanForEveryPrefabType() {
        for (PrefabType prefabType : PrefabType.values()) {
            PrefabInitializer initializer =
                    context.getBean(prefabType.getBeanName(), PrefabInitializer.class);

            assertThat(initializer.prefabType)
                    .as("bean %s initializes %s", prefabType.getBeanName(), prefabType)
                    .isEqualTo(prefabType);
        }
    }

    @Test
    void noInitializerBeanIsUnreachableFromPrefabType() {
        Set<String> namesPrefabTypeAsksFor = Arrays.stream(PrefabType.values())
                .map(PrefabType::getBeanName)
                .collect(Collectors.toSet());

        assertThat(context.getBeansOfType(PrefabInitializer.class).keySet())
                .containsExactlyInAnyOrderElementsOf(namesPrefabTypeAsksFor);
    }
}
