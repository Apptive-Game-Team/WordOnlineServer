package com.wordonline.server.game.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

public class SynchronousFlowPublisher<T> implements Flow.Publisher<T>, AutoCloseable {
    private final List<SynchronousSubscription> subscriptions = new CopyOnWriteArrayList<>();
    private boolean closed;

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        if (closed) {
            subscriber.onSubscribe(new ClosedSubscription());
            subscriber.onComplete();
            return;
        }

        SynchronousSubscription subscription = new SynchronousSubscription(subscriber);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void publish(T event) {
        if (closed) {
            return;
        }
        subscriptions.forEach(subscription -> subscription.publish(event));
    }

    @Override
    public void close() {
        closed = true;
        subscriptions.forEach(SynchronousSubscription::complete);
        subscriptions.clear();
    }

    private class SynchronousSubscription implements Flow.Subscription {
        private final Flow.Subscriber<? super T> subscriber;
        private long demand;
        private boolean canceled;

        private SynchronousSubscription(Flow.Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            if (n <= 0 || canceled) {
                return;
            }
            demand = Math.min(Long.MAX_VALUE, demand + n);
        }

        @Override
        public void cancel() {
            canceled = true;
            subscriptions.remove(this);
        }

        private void publish(T event) {
            if (canceled || demand <= 0) {
                return;
            }
            demand--;
            subscriber.onNext(event);
        }

        private void complete() {
            if (canceled) {
                return;
            }
            canceled = true;
            subscriber.onComplete();
        }
    }

    private static class ClosedSubscription implements Flow.Subscription {
        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
    }
}
