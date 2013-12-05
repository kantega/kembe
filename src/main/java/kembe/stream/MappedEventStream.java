package kembe.stream;

import fj.F;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

public class MappedEventStream<A, B> extends EventStream<B> {

    private final EventStream<A> mapped;
    private final F<A, B> f;

    public MappedEventStream(EventStream<A> mapped, F<A, B> f) {
        this.mapped = mapped;
        this.f = f;
    }

    @Override
    public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        final OpenEventStream<A> oepA =
                mapped.open(effect.comap(f));

        return OpenEventStream.wrap(this, oepA);
    }
}
