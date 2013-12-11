package kembe.stream;

import kembe.EventStream;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

public class TapEventStream<A> extends EventStream<A> {

    private final EventStream<A> source;

    private final EventStreamSubscriber<A> tap;

    public TapEventStream(EventStream<A> source, EventStreamSubscriber<A> tap) {
        this.source = source;
        this.tap = tap;
    }

    @Override public OpenEventStream<A> open(final EventStreamSubscriber<A> subscriber) {
        return source.open( EventStreamSubscriber.create( new EventStreamHandler<A>() {
            @Override public void next(A a) {
                tap.next( a );
                subscriber.next( a );
            }

            @Override public void error(Exception e) {
                tap.error( e );
                subscriber.error( e );
            }

            @Override public void done() {
                tap.done();
                subscriber.done();
            }
        } ) );
    }
}
