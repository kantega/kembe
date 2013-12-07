package kembe.stream;

import fj.Effect;
import kembe.*;

public class MealyEventStream<A, B> extends EventStream<B> {

    private final State<A, B> initialState;

    private final EventStream<A> source;

    public MealyEventStream(EventStream<A> source, State<A, B> initialState) {
        this.initialState = initialState;
        this.source = source;
    }

    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> open =
                source.open(
                        effect.<A>onNext(
                                new Effect<A>() {
                                    volatile State<A, B> state = initialState;

                                    @Override
                                    public void e(A next) {
                                        try {
                                            State.Transition<A, B> t = state.apply( next );
                                            state = t.nextState;
                                            effect.e( StreamEvent.next( t.result ) );
                                        } catch (Exception e) {
                                            effect.e( StreamEvent.<B>error( e ) );
                                        }
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
