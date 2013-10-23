package org.kantega.kembe.stream;

import fj.Effect;
import fj.data.Either;
import fj.data.List;
import org.kantega.kembe.EventStream;
import org.kantega.kembe.EventStreamSubscriber;
import org.kantega.kembe.OpenEventStream;
import org.kantega.kembe.StreamEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AndThenEventStream<A> extends EventStream<A> {

    private final EventStream<A> first;
    private final EventStream<A> eventual;

    public AndThenEventStream(EventStream<A> first, EventStream<A> eventual) {
        this.first = first;
        this.eventual = eventual;
    }

    @Override
    public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {


        final Effect<Either<StreamEvent<A>, StreamEvent<A>>> bufferingEffect =
                new Effect<Either<StreamEvent<A>, StreamEvent<A>>>() {
                    final ArrayList<A> buffer =
                            new ArrayList<A>();
                    final AtomicBoolean flushed =
                            new AtomicBoolean(false);
                    final Effect<StreamEvent<A>> eventualHandler =
                            EventStreamSubscriber.wrap( effect ).onNext(
                                    new Effect<StreamEvent.Next<A>>() {
                                        @Override
                                        public void e(StreamEvent.Next<A> next) {
                                            if (flushed.get())
                                                effect.e(new StreamEvent.Next<A>(next.value));
                                            else
                                                buffer.add(next.value);
                                        }
                                    }
                            );
                    final Effect<StreamEvent<A>> firstHandler =
                            EventStreamSubscriber.wrap(effect).onDone(
                                    new Effect<StreamEvent.Done<A>>() {
                                        public void e(StreamEvent.Done<A> objectDone) {
                                            flushed.set(true);
                                            for (A a : buffer) {
                                                effect.e(new StreamEvent.Next<A>(a));
                                            }
                                            buffer.clear();
                                        }
                                    }
                            );

                    @Override
                    public void e(Either<StreamEvent<A>, StreamEvent<A>> event) {
                        if (event.isLeft()) {
                            firstHandler.e(event.left().value());
                        } else {
                            eventualHandler.e(event.right().value());
                        }
                    }
                };

        OpenEventStream<A> firstOpenStream = first.open(bufferingEffect.comap(Either.<StreamEvent<A>, StreamEvent<A>>left_()));
        OpenEventStream<A> eventualOpenStream = eventual.open(bufferingEffect.comap(Either.<StreamEvent<A>, StreamEvent<A>>right_()));

        return OpenEventStream.wrap(this, List.<OpenEventStream<?>>list(firstOpenStream, eventualOpenStream));
    }
}
