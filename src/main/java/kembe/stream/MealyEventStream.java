package kembe.stream;

import fj.Effect;
import kembe.*;

public class MealyEventStream<A, B> extends EventStream<B> {

    private final State<A, B> initialState;

    private final EventStream<A> source;

    public MealyEventStream(EventStream<A> source,State<A, B> initialState) {
        this.initialState = initialState;
        this.source = source;
    }

    @Override public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect) {
        OpenEventStream<A> open =
                source.open(
                        EventStreamSubscriber.forwardTo( effect ).<A>onNext(
                                new Effect<StreamEvent.Next<A>>() {
                                    volatile State<A, B> state = initialState;

                                    @Override
                                    public void e(StreamEvent.Next<A> next) {
                                        State.Transition<A, B> t = state.apply( next.value );
                                        state = t.nextState;
                                        effect.e( StreamEvent.next( t.result ) );
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
