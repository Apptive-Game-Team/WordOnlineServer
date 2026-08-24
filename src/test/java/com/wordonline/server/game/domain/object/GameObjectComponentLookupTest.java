package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GameObjectComponentLookupTest {

    @Test
    void findsAComponentOfTheRequestedType() {
        GameObject gameObject = newGameObject();
        MarkerComponent marker = addComponent(gameObject, new MarkerComponent(gameObject));

        assertThat(gameObject.hasComponent(MarkerComponent.class)).isTrue();
        assertThat(gameObject.getComponent(MarkerComponent.class)).isSameAs(marker);
        assertThat(gameObject.getComponentOptional(MarkerComponent.class)).containsSame(marker);
        assertThat(gameObject.getComponents(MarkerComponent.class)).containsExactly(marker);
    }

    @Test
    void reportsNothingWhenNoComponentMatches() {
        GameObject gameObject = newGameObject();
        addComponent(gameObject, new OtherComponent(gameObject));

        assertThat(gameObject.hasComponent(MarkerComponent.class)).isFalse();
        assertThat(gameObject.getComponent(MarkerComponent.class)).isNull();
        assertThat(gameObject.getComponentOptional(MarkerComponent.class)).isEmpty();
        assertThat(gameObject.getComponents(MarkerComponent.class)).isEmpty();
    }

    @Test
    void reusesTheSameEmptyListInsteadOfAllocatingOnePerCall() {
        GameObject gameObject = newGameObject();
        addComponent(gameObject, new OtherComponent(gameObject));

        List<MarkerComponent> first = gameObject.getComponents(MarkerComponent.class);
        List<MarkerComponent> second = gameObject.getComponents(MarkerComponent.class);

        assertThat(first).isSameAs(second);
        assertThat(first).isSameAs(List.of());
    }

    @Test
    void returnsEveryMatchInInsertionOrder() {
        GameObject gameObject = newGameObject();
        MarkerComponent first = addComponent(gameObject, new MarkerComponent(gameObject));
        addComponent(gameObject, new OtherComponent(gameObject));
        MarkerComponent second = addComponent(gameObject, new MarkerComponent(gameObject));
        MarkerComponent third = addComponent(gameObject, new MarkerComponent(gameObject));

        assertThat(gameObject.getComponents(MarkerComponent.class))
                .containsExactly(first, second, third);
        assertThat(gameObject.getComponent(MarkerComponent.class)).isSameAs(first);
    }

    @Test
    void matchesSubclassesTheWayClassIsInstanceDoes() {
        GameObject gameObject = newGameObject();
        OtherComponent other = addComponent(gameObject, new OtherComponent(gameObject));
        SubMarkerComponent subMarker = addComponent(gameObject, new SubMarkerComponent(gameObject));

        assertThat(gameObject.hasComponent(MarkerComponent.class)).isTrue();
        assertThat(gameObject.getComponent(MarkerComponent.class)).isSameAs(subMarker);
        assertThat(gameObject.getComponents(MarkerComponent.class)).containsExactly(subMarker);
        assertThat(gameObject.getComponents(Component.class)).containsExactly(other, subMarker);
        assertThat(gameObject.hasComponent(SubMarkerComponent.class)).isTrue();
    }

    @Test
    void rejectsMutationOfTheReturnedList() {
        GameObject gameObject = newGameObject();
        MarkerComponent marker = addComponent(gameObject, new MarkerComponent(gameObject));

        List<MarkerComponent> matches = gameObject.getComponents(MarkerComponent.class);

        assertThatThrownBy(() -> matches.add(marker))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> matches.remove(0))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> gameObject.getComponents(MarkerComponent.class).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void isNotDisturbedByComponentsQueuedForAdditionOrRemovalDuringUpdate() {
        GameObject gameObject = newGameObject();
        MarkerComponent marker = addComponent(gameObject, new MarkerComponent(gameObject));
        // a component that touches the component list from inside update(), the way real ones do
        addComponent(gameObject, new SelfMutatingComponent(gameObject));

        gameObject.update();

        assertThat(gameObject.getComponents(MarkerComponent.class)).containsExactly(marker);
        gameObject.flushComponents();
        assertThat(gameObject.getComponents(MarkerComponent.class)).hasSize(2);
    }

    private static GameObject newGameObject() {
        return new GameObject(Master.LeftPlayer, PrefabType.ZapMouse, Vector3.ZERO, mock(GameContext.class));
    }

    private static <T extends Component> T addComponent(GameObject gameObject, T component) {
        gameObject.getComponents().add(component);
        return component;
    }

    private static class MarkerComponent extends Component {
        private MarkerComponent(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
        }

        @Override
        public void onDestroy() {
        }
    }

    private static class SubMarkerComponent extends MarkerComponent {
        private SubMarkerComponent(GameObject gameObject) {
            super(gameObject);
        }
    }

    private static class OtherComponent extends Component {
        private OtherComponent(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
        }

        @Override
        public void onDestroy() {
        }
    }

    private static class SelfMutatingComponent extends Component {
        private SelfMutatingComponent(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
            gameObject.getComponents(MarkerComponent.class);
            gameObject.hasComponent(MarkerComponent.class);
            gameObject.getComponent(MarkerComponent.class);
            gameObject.addComponent(new MarkerComponent(gameObject));
            gameObject.removeComponent(this);
        }

        @Override
        public void onDestroy() {
        }
    }
}
