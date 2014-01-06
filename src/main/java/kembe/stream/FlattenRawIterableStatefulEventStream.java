package kembe.stream;

import kembe.*;

public class FlattenRawIterableStatefulEventStream<A> extends EventStream<A> {

    private final Mealy<StreamEvent<A>, Iterable<StreamEvent<A>>> initialMealy;

    private final EventStream<A> source;

    public FlattenRawIterableStatefulEventStream(EventStream<A> source, Mealy<StreamEvent<A>, Iterable<StreamEvent<A>>> initialMealy) {
        this.initialMealy = initialMealy;
        this.source = source;
    }

    @Override public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {
        OpenEventStream<A> open =
                source.open( new EventStreamSubscriber<A>() {
                    volatile Mealy<StreamEvent<A>, Iterable<StreamEvent<A>>> state = initialMealy;

                    @Override public void e(StreamEvent<A> event) {
                        try {
                            Mealy.Transition<StreamEvent<A>, Iterable<StreamEvent<A>>> t = state.apply( event );
                            state = t.nextMealy;
                            for(StreamEvent<A> e:t.result){
                                effect.e( e );
                            }
                        } catch (Exception e) {
                            effect.e( StreamEvent.<A>error( e ) );
                        }
                    }
                } );

        return OpenEventStream.wrap( this, open );
    }
}
