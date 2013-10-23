package org.kantega.falls.stream;

import fj.Effect;
import fj.F;
import org.kantega.falls.EventStream;
import org.kantega.falls.EventStreamSubscriber;
import org.kantega.falls.OpenEventStream;
import org.kantega.falls.StreamEvent;

public class FilterEventStream<A> extends EventStream<A>
{
    private final EventStream<A> wrapped;

    private final F<A, Boolean> predicate;

    public FilterEventStream(EventStream<A> wrapped, F<A, Boolean> predicate)
    {
        this.wrapped = wrapped;
        this.predicate = predicate;
    }

    @Override
    public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect)
    {
        OpenEventStream<A> wo =
                wrapped.open(EventStreamSubscriber.<A>wrap(effect).onNext(new Effect<StreamEvent.Next<A>>()
                {
                    @Override
                    public void e(StreamEvent.Next<A> next)
                    {
                        if (predicate.f(next.value))
                        { effect.e(next); }
                    }
                }));

        return OpenEventStream.wrap(this, wo);
    }
}
