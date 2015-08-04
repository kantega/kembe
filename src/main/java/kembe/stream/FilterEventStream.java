package kembe.stream;

import fj.F;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

public class FilterEventStream<A> extends EventStream<A> {
    private final EventStream<A> wrapped;

    private final F<A, Boolean> predicate;

    public FilterEventStream(EventStream<A> wrapped, F<A, Boolean> predicate) {
        this.wrapped = wrapped;
        this.predicate = predicate;
    }

    @Override
    public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {

        return wrapped.open( effect.onNext( next -> {
            if (predicate.f( next )) {
                effect.next( next );
            }
        } ) );

    }
}
