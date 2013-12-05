package kembe.stream;

import fj.F;
import fj.Unit;
import kembe.*;
import kembe.util.Effects;

public class BoundEventStream<A, B> extends EventStream<B> {
    private final F<A, EventStream<B>> f;

    private final EventStream<A> bound;

    public BoundEventStream(EventStream<A> bound, F<A, EventStream<B>> f) {
        this.f = f;
        this.bound = bound;
    }

    @Override
    public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> a =
                bound.open(
                        EventStreamSubscriber.create( new EventStreamHandler<A>() {
                            @Override
                            public void next(A a) {
                                EventStream<B> bs = f.f( a );
                                bs.open(
                                        effect.onDone(
                                                Effects.<Unit>noOp()
                                        )
                                );
                            }

                            @Override
                            public void error(Exception e) {
                                effect.e( StreamEvent.<B>error( e ) );
                            }

                            @Override
                            public void done() {
                                effect.e( StreamEvent.<B>done() );
                            }
                        } ) );

        return OpenEventStream.wrap( this, a );
    }
}
