package kembe.stream;

import fj.Effect;
import fj.F;
import fj.data.Option;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class OptionalEventStream<A, B> extends EventStream<B> {

    private final EventStream<A> source;

    private final F<A, Option<B>> f;

    public OptionalEventStream(EventStream<A> source, F<A, Option<B>> f) {
        this.source = source;
        this.f = f;
    }

    @Override public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect) {
        OpenEventStream<A> open =
                source.open(
                        EventStreamSubscriber.forwardTo(effect).<A>onNext(
                                new Effect<StreamEvent.Next<A>>()
                                {
                                    @Override
                                    public void e(StreamEvent.Next<A> next)
                                    {
                                        Option<B> maybeB = f.f(next.value);
                                        if (maybeB.isSome())
                                         effect.e(StreamEvent.next(maybeB.some()));
                                    }
                                }));

        return OpenEventStream.wrap( this, open );
    }
}
