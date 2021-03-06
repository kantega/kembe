package kembe.stream;

import fj.Unit;
import fj.function.Effect1;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

public class AppendEventStream<A> extends EventStream<A> {

    private final EventStream<A> one;

    private final EventStream<A> other;

    public AppendEventStream(EventStream<A> one, EventStream<A> other) {
        this.one = one;
        this.other = other;
    }

    @Override public OpenEventStream<A> open(final EventStreamSubscriber<A> subscriber) {
        return one.open( subscriber.onDone( unit -> other.open( subscriber ) ) );
    }
}
