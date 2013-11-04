package kembe.stream;

import fj.Effect;
import fj.F;
import kembe.EventStream;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class MappedEventStream<A, B> extends EventStream<B> {

    private final EventStream<A> mapped;
    private final F<A, B> f;

    public MappedEventStream(EventStream<A> mapped, F<A, B> f) {
        this.mapped = mapped;
        this.f = f;
    }

    @Override
    public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect) {
        final OpenEventStream<A> oepA =
                mapped.open(effect.comap(StreamEvent.lift(f)));

        return OpenEventStream.wrap(this, oepA);
    }
}