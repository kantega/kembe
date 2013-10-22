package org.kantega.falls.pipes;

import fj.Effect;
import fj.data.Either;
import fj.data.List;
import org.kantega.falls.EventStream;
import org.kantega.falls.OpenEventStream;
import org.kantega.falls.StreamEvent;

public class EitherEventStream<A, B> extends EventStream<Either<A, B>>
{
    private final EventStream<A> one;

    private final EventStream<B> other;

    public EitherEventStream(EventStream<A> one, EventStream<B> other)
    {
        this.one = one;
        this.other = other;
    }



    @Override
    public OpenEventStream<Either<A, B>> open(final Effect<StreamEvent<Either<A, B>>> effect)
    {
        final OpenEventStream<A> oA = one.open(effect.comap(StreamEvent.lift(Either.<A, B>left_())));
        final OpenEventStream<B> oB = other.open(effect.comap(StreamEvent.lift(Either.<A, B>right_())));

        return OpenEventStream.wrap(this, List.list(oA, oB));
    }
}
