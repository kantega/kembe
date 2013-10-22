package org.kantega.falls;

import fj.Effect;

public class EventStreamSubscriber<A> extends Effect<StreamEvent<A>> {

    final Effect<StreamEvent.Next<A>> onNext;
    final Effect<StreamEvent.Error<A>> onException;
    final Effect<StreamEvent.Done<A>> onDone;

    protected EventStreamSubscriber(
            Effect<StreamEvent.Next<A>> onNext,
            Effect<StreamEvent.Error<A>> onException,
            Effect<StreamEvent.Done<A>> onDone) {
        this.onNext = onNext;
        this.onException = onException;
        this.onDone = onDone;
    }

    public static <A> EventStreamSubscriber<A> wrap(Effect<StreamEvent<A>> effect) {
        return EventStreamSubscriber.create(
                EventStreamSubscriber.forwardTo(effect),
                EventStreamSubscriber.forwardTo(effect),
                EventStreamSubscriber.forwardTo(effect));
    }

    public static <A> EventStreamSubscriber<A> create(
            final Effect<StreamEvent.Next<A>> onNext,
            final Effect<StreamEvent.Error<A>> onException,
            final Effect<StreamEvent.Done<A>> onDone) {
        return new EventStreamSubscriber<>(onNext, onException, onDone);

    }

    public static <A, E extends StreamEvent<A>> Effect<E> forwardTo(final Effect<StreamEvent<A>> effect) {
        return new Effect<E>() {
            @Override
            public void e(E aNext) {
                effect.e(aNext);
            }
        };
    }

    public static <A> Effect<StreamEvent<A>> noOp() {
        return new Effect<StreamEvent<A>>() {
            @Override
            public void e(StreamEvent<A> aStreamEvent) {

            }
        };
    }

    public EventStreamSubscriber<A> onNext(final Effect<? super StreamEvent.Next<A>> onNext) {
        return EventStreamSubscriber.create(onNext, this.onException, this.onDone);
    }

    public EventStreamSubscriber<A> onDone(final Effect<? super StreamEvent.Done<A>> onDone) {
        return EventStreamSubscriber.create(this.onNext, this.onException, onDone);
    }

    @Override
    public void e(StreamEvent<A> aStreamEvent) {
        aStreamEvent
                .effect(
                        new Effect<StreamEvent.Next<A>>() {
                            @Override
                            public void e(StreamEvent.Next<A> aNext) {
                                onNext.e(aNext);
                            }
                        }, new Effect<StreamEvent.Error<A>>() {
                            @Override
                            public void e(StreamEvent.Error<A> err) {
                                onException.e(err);
                            }
                        }, new Effect<StreamEvent.Done<A>>() {
                            @Override
                            public void e(StreamEvent.Done<A> done) {
                                onDone.e(done);
                            }
                        }
                );

    }
}
