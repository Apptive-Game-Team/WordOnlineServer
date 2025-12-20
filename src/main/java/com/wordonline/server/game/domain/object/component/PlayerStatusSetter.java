package com.wordonline.server.game.domain.object.component;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.input.InputHandleEvent;

public class PlayerStatusSetter extends Component implements Flow.Subscriber<InputHandleEvent> {

    private Subscription subscription;

    public PlayerStatusSetter(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {
        getGameContext().getMagicInputHandler().subscribe(this);
    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(InputHandleEvent item) {
        subscription.request(1);
        handleInputEvent(item);
    }

    private void handleInputEvent(InputHandleEvent event) {
        if (!event.master().equals(gameObject.getMaster())) {
            return;
        }

        switch (event.ResultCode()) {
            case SUCCESS -> getGameObject().setStatus(Status.Attack);
            case FAIL_INVALID_MAGIC -> getGameObject().setStatus(Status.Hindered);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
