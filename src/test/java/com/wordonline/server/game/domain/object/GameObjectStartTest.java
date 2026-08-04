package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameObjectStartTest {

    @Test
    void startsEachComponentExactlyOnceRegardlessOfHowItWasRegistered() {
        List<CountingComponent> created = new ArrayList<>();

        GameObject gameObject = startWith(go -> {
            CountingComponent direct = new CountingComponent(go);
            go.getComponents().add(direct);
            CountingComponent queued = new CountingComponent(go);
            go.addComponent(queued);
            created.add(direct);
            created.add(queued);
        });

        assertThat(created).allMatch(component -> component.starts == 1);
        assertThat(gameObject.getComponents()).containsExactlyElementsOf(created);
    }

    private GameObject startWith(Consumer<GameObject> initializeBody) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(anyString(), eq(PrefabInitializer.class)))
                .thenReturn(new StubPrefabInitializer(initializeBody));
        new PrefabProvider().setApplicationContext(applicationContext);

        GameObject gameObject = new GameObject(
                Master.LeftPlayer, PrefabType.ZapMouse, Vector3.ZERO, mock(GameContext.class));
        gameObject.start();
        return gameObject;
    }

    private static class StubPrefabInitializer extends PrefabInitializer {
        private final Consumer<GameObject> initializeBody;

        private StubPrefabInitializer(Consumer<GameObject> initializeBody) {
            super(PrefabType.ZapMouse);
            this.initializeBody = initializeBody;
        }

        @Override
        public void initialize(GameObject gameObject) {
            initializeBody.accept(gameObject);
        }
    }

    private static class CountingComponent extends Component {
        private int starts;

        private CountingComponent(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void start() {
            starts++;
        }

        @Override
        public void update() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
