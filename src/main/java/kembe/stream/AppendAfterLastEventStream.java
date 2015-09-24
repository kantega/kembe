package kembe.stream;

import fj.F;
import kembe.EventStream;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

import java.util.concurrent.atomic.AtomicReference;

public class AppendAfterLastEventStream<A> extends EventStream<A> {

    final EventStream<A> first;

    final F<A, EventStream<A>> next;

    public AppendAfterLastEventStream(EventStream<A> first, F<A, EventStream<A>> next) {
        this.first = first;
        this.next = next;
    }

    @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
        AtomicReference<OpenEventStream<A>> secondOpen = new AtomicReference<>( null );
        OpenEventStream<A> firstOpen = first.open( EventStreamSubscriber.create( new EventStreamHandler<A>() {

            A last = null;

            @Override public void next(A a) {
                last = a;
                subscriber.next( a );
            }

            @Override public void error(Exception e) {
                subscriber.error( e );
            }

            @Override public void done() {
                if (last != null)
                    secondOpen.set( next.f( last ).open( subscriber ) );
                else
                    subscriber.done();
            }
        } ) );

        //Not threadsafe
        return new OpenEventStream<A>() {
            @Override public EventStream<A> close() {
                firstOpen.close();
                if (secondOpen.get() != null) secondOpen.get().close();
                return new AppendAfterLastEventStream<>( first, next );
            }
        };
    }
}
