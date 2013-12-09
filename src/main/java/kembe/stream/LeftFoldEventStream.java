package kembe.stream;

import fj.Effect;
import fj.F2;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class LeftFoldEventStream<A, B> extends EventStream<B> {

    private final F2<B,A, B> f;

    private final B initState;

    private final EventStream<A> source;

    public LeftFoldEventStream(F2<B, A, B> f, B initState, EventStream<A> source) {
        this.f = f;
        this.initState = initState;
        this.source = source;
    }


    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> open =
                source.open(
                        effect.<A>onNext(
                                new Effect<A>() {
                                    volatile B state = initState;

                                    @Override
                                    public void e(A next) {
                                        try {
                                            state = f.f(state).f(next);
                                            effect.e( StreamEvent.next( state) );
                                        } catch (Exception e) {
                                            effect.e( StreamEvent.<B>error( e ) );
                                        }
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
