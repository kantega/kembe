package kembe.stream;

import kembe.*;

public class FlattenRawIterableStatefulEventStream<A, B> extends EventStream<B> {

    private final Mealy<StreamEvent<A>, EventStream<B>> initialMealy;

    private final EventStream<A> source;

    public FlattenRawIterableStatefulEventStream(EventStream<A> source, Mealy<StreamEvent<A>, EventStream<B>> initialMealy) {
        this.initialMealy = initialMealy;
        this.source = source;
    }

    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> open =
                source.open( new EventStreamSubscriber<A>() {
                    volatile Mealy<StreamEvent<A>, EventStream<B>> state = initialMealy;

                    @Override public void e(StreamEvent<A> event) {
                        Mealy.Transition<StreamEvent<A>, EventStream<B>> t = state.apply( event );
                        state = t.nextMealy;
                        t.result.open( EventStreamSubscriber.create( new EventStreamHandler<B>() {
                            @Override public void next(B b) {
                                effect.next( b );
                            }

                            @Override public void error(Exception e) {
                                effect.error( e );
                            }

                            @Override public void done() {
                                //Althoug this stream is done, the original stream might have more events.
                                //We only send done if the source sends done.
                            }
                        } ) );
                        //Handle errors and propagate done
                        event.effect( new EventStreamHandler() {
                            @Override public void next(Object o) {
                                //Noop, handled above
                            }

                            @Override public void error(Exception e) {
                                //Propagate errors
                                effect.error( e );
                            }

                            @Override public void done() {
                                effect.done();
                            }
                        } );

                    }
                } );

        return OpenEventStream.wrap( this, open );
    }
}
