package kembe;

import fj.Effect;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventStreamConnector<A> {

    private final CopyOnWriteArrayList<EventStreamSubscriber<A>> observers = new CopyOnWriteArrayList<>();



    public EventStream<A> stream(){
        return new EventStream<A>() {
            @Override public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {
                observers.add( effect );
                final EventStream<A> self = this;
                return new OpenEventStream<A>() {
                    @Override public EventStream<A> close() {
                        observers.remove( effect );
                        return self;
                    }
                };
            }
        };
    }

    public EventStreamSubscriber<A> subscriber(){
        return EventStreamSubscriber.create( toEffect() );
    }

    public void next(A a) {
        submit( StreamEvent.next( a ) );
    }

    public void error(Exception e) {
        submit( StreamEvent.<A>error( e ) );
    }

    public void done() {
        submit( StreamEvent.<A>done() );
    }

    public Effect<StreamEvent<A>> toEffect() {
        return new Effect<StreamEvent<A>>() {
            @Override public void e(StreamEvent<A> evt) {
                submit( evt );
            }
        };
    }

    private void submit(StreamEvent<A> event) {
        for (EventStreamSubscriber<A> observer : observers) {
            observer.e( event );
        }
    }
}
