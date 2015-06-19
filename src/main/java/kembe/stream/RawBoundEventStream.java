package kembe.stream;

import fj.F;
import fj.function.Effect1;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class RawBoundEventStream<A, B> extends EventStream<B> {

    private final EventStream<A> bound;

    private final F<StreamEvent<A>, EventStream<B>> f;

    public RawBoundEventStream(EventStream<A> bound, F<StreamEvent<A>, EventStream<B>> f) {
        this.bound = bound;
        this.f = f;
    }

    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> subscriber) {
        OpenEventStream<A> openA = bound.open( EventStreamSubscriber.create( aStreamEvent -> {
            EventStream<B> bs = f.f( aStreamEvent );
            bs.open( subscriber );
        } ) );

        return OpenEventStream.wrap( this, openA );
    }
}
