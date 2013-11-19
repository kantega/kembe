package kembe.stream;

import fj.Effect;
import fj.F;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class FilterEventStream<A> extends EventStream<A> {
    private final EventStream<A> wrapped;

    private final F<A, Boolean> predicate;

    public FilterEventStream(EventStream<A> wrapped, F<A, Boolean> predicate) {
        this.wrapped = wrapped;
        this.predicate = predicate;
    }

    @Override
    public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {

        return wrapped.open( EventStreamSubscriber.<A>forwardTo( effect ).onNext( new Effect<StreamEvent.Next<A>>() {
            @Override
            public void e(StreamEvent.Next<A> next) {
                if (predicate.f( next.value )) {
                    effect.e( next );
                }
            }
        } ) );

    }
}
