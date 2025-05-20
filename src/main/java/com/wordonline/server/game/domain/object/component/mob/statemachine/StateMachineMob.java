package com.wordonline.server.game.domain.object.component.mob.statemachine;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Status;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public abstract class StateMachineMob extends Mob {

    private State currentState;
    private final List<State> states = getStates();

    private List<State> getStates() {
        return Arrays.stream(this.getClass().getDeclaredClasses())
                .filter(stateClass ->
                        State.class.isAssignableFrom(stateClass) &&
                                !Modifier.isAbstract(stateClass.getModifiers())
                )
                .map(stateClass -> {
                    try {
                        State state = (State) stateClass.getDeclaredConstructor(this.getClass()).newInstance(this);
                        return state;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(state -> state != null)
                .toList();
    }

    public void setState(State newState) {
        if (currentState != null) {
            currentState.onExit();
        }
        currentState = newState;
        currentState.onEnter();
    }

    public void update() {
        if (currentState != null) {
            currentState.onUpdate();
        }
    }

    public void onDestroy() {
        if (currentState != null) {
            currentState.onExit();
        }
    }

    public abstract class State {
        public State() {}

        public abstract void onEnter();

        public abstract void onExit();

        public abstract void onUpdate();
    }

    public StateMachineMob(GameObject gameObject, int maxHp, int speed) {
        super(gameObject, maxHp, speed);
    }
}
