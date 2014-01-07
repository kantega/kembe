package kembe.stream;

import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EventStreamFork<A>{

    private final EventStream<A> source;
    private CopyOnWriteArrayList<EventStreamSubscriber<A>> subscribers = new CopyOnWriteArrayList<>(  );
    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger opened = new AtomicInteger( 0 );

    public EventStreamFork(EventStream<A> source) {
        this.source = source;
    }

    public EventStream<A> newStream(){
        count.incrementAndGet();
        EventStream<A> es = new EventStream<A>() {
            @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
                subscribers.add( subscriber );
                if(opened.incrementAndGet()==count.get()){
                    source.open( new EventStreamSubscriber<A>() {
                        @Override public void e(StreamEvent<A> event) {
                            distribute( event );
                        }
                    } );
                }
                return OpenEventStream.noOp( this );
            }
        };
        return es;
    }


    public void distribute(StreamEvent<A> event) {
        for(EventStreamSubscriber<A> s:subscribers){
            s.e( event );
        }
    }
}
