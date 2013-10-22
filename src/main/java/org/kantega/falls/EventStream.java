package org.kantega.falls;

import fj.Effect;
import fj.F;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import org.kantega.falls.pipes.*;

public abstract class EventStream<A> {

    public abstract OpenEventStream<A> open(Effect<StreamEvent<A>> effect);

    public <B> EventStream<B> map(final F<A, B> f) {
        return new MappedEventStream<>(this, f);
    }

    public <B> EventStream<B> bind(F<A, EventStream<B>> f) {
        return new BoundEventStream<>(this, f);
    }

    public <B> EventStream<Either<A, B>> either(EventStream<B> other) {
        return new EitherEventStream<>(this, other);
    }

    public EventStream<A> merge(EventStream<A> other) {
        return EitherEventStream.normalize(new EitherEventStream<>(this, other));
    }

    public EventStream<A> andThen(EventStream<A> eventual) {
        return new AndThenEventStream<>(this, eventual);
    }

    public <B> EventStream<P2<A, List<B>>> join(EventStream<B> other) {
        return new JoinEventStream<>(this, other);
    }

}
