package org.kantega.falls.pipes;

import fj.Effect;
import fj.F;
import org.kantega.falls.EventStream;
import org.kantega.falls.EventStreamSubscriber;
import org.kantega.falls.OpenEventStream;
import org.kantega.falls.StreamEvent;

public class BoundEventStream<A, B> extends EventStream<B> {

    private final F<A, EventStream<B>> f;
    private final EventStream<A> bound;

    public BoundEventStream(EventStream<A> bound, F<A, EventStream<B>> f) {
        this.f = f;
        this.bound = bound;
    }

    @Override
    public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect) {
        OpenEventStream<A> a =
                bound.open(
                        EventStreamSubscriber.<A>wrap( effect ).onNext(
                                new Effect<StreamEvent.Next<A>>() {
                                    public void e(StreamEvent.Next<A> next) {
                                        EventStream<B> bs = f.f(next.value);
                                        bs.open(
                                                EventStreamSubscriber.wrap(effect).onDone(
                                                        EventStreamSubscriber.<B>noOp()
                                                )
                                        );
                                    }
                                }
                        )
                );


        return OpenEventStream.wrap(this, a);
    }
}
