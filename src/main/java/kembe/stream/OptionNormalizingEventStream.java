package kembe.stream;

import fj.Effect;
import fj.data.Option;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class OptionNormalizingEventStream<A> extends EventStream<A> {

    private final EventStream<Option<A>> source;


    public OptionNormalizingEventStream(EventStream<Option<A>> source) {
        this.source = source;
    }

    @Override public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {
        OpenEventStream<Option<A>> open =
                source.open(
                        EventStreamSubscriber.forwardTo(effect).<Option<A>>onNext(
                                new Effect<StreamEvent.Next<Option<A>>>()
                                {
                                    @Override
                                    public void e(StreamEvent.Next<Option<A>> next)
                                    {
                                        Option<A> maybeB = next.value;
                                        if (maybeB.isSome())
                                         effect.e(StreamEvent.next(maybeB.some()));
                                    }
                                }));

        return OpenEventStream.wrap( this, open );
    }
}
