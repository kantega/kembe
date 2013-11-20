package kembe;

import fj.Effect;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventStreamSource<A> extends EventStream<A> {

    private final CopyOnWriteArrayList<Effect<StreamEvent<A>>> observers = new CopyOnWriteArrayList<>();

    @Override public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {
        observers.add( effect );
        final EventStreamSource<A> self = this;
        return new OpenEventStream<A>() {
            @Override public EventStream<A> close() {
                observers.remove( effect );
                return self;
            }
        };
    }

    public void next(A a) {
        submit( StreamEvent.next( a ) );
    }

    public void error(Exception e){
        submit( StreamEvent.<A>error( e ) );
    }

    public void done(){
        submit( StreamEvent.<A>done() );
    }

    private void submit(StreamEvent<A> event) {
        for (Effect<StreamEvent<A>> observer : observers) {
            observer.e( event );
        }
    }
}
