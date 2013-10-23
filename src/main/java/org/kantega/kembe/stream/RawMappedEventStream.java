package org.kantega.kembe.stream;

import fj.Effect;
import fj.F;
import org.kantega.kembe.EventStream;
import org.kantega.kembe.OpenEventStream;
import org.kantega.kembe.StreamEvent;

public class RawMappedEventStream<A,B> extends EventStream<B>{
    private final EventStream<A> mapped;
    private final F<StreamEvent<A>,StreamEvent<B>> f;

    public RawMappedEventStream(EventStream<A> mapped, F<StreamEvent<A>,StreamEvent<B>> f) {
        this.mapped = mapped;
        this.f = f;
    }

    @Override
    public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect) {
        final OpenEventStream<A> oepA =
                mapped.open(effect.comap(f));

        return OpenEventStream.wrap(this, oepA);
    }
}
