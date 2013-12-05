package kembe.stream;

import fj.F;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class RawMappedEventStream<A,B> extends EventStream<B>{
    private final EventStream<A> mapped;
    private final F<StreamEvent<A>,StreamEvent<B>> f;

    public RawMappedEventStream(EventStream<A> mapped, F<StreamEvent<A>,StreamEvent<B>> f) {
        this.mapped = mapped;
        this.f = f;
    }

    @Override
    public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        final OpenEventStream<A> oepA =
                mapped.open(effect.comapEvent(f));

        return OpenEventStream.wrap(this, oepA);
    }
}
