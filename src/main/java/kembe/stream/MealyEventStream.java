package kembe.stream;

import fj.Effect;
import fj.function.Effect1;
import kembe.*;

public class MealyEventStream<A, B> extends EventStream<B> {

    private final Mealy<A, B> initialMealy;

    private final EventStream<A> source;

    public MealyEventStream(EventStream<A> source, Mealy<A, B> initialMealy) {
        this.initialMealy = initialMealy;
        this.source = source;
    }

    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> open =
                source.open(
                        effect.onNext(
                                new Effect1<A>() {
                                    volatile Mealy<A, B> state = initialMealy;

                                    @Override
                                    public void f(A next) {
                                        try {
                                            Mealy.Transition<A, B> t = state.apply( next );
                                            state = t.nextMealy;
                                            effect.e( StreamEvent.next( t.result ) );
                                        } catch (Exception e) {
                                            effect.e( StreamEvent.<B>error( e ) );
                                        }
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
