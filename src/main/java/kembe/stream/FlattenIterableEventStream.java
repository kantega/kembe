package kembe.stream;

import fj.F;
import kembe.*;

public class FlattenIterableEventStream<A,B> extends EventStream<B> {

    private final F<A, Iterable<B>> f;

    private final EventStream<A> source;

    public FlattenIterableEventStream(EventStream<A> source, F<A, Iterable<B>> f) {
        this.f = f;
        this.source = source;
    }

    @Override public OpenEventStream<B> open(final EventStreamSubscriber<B> effect) {
        OpenEventStream<A> open =
                source.open( EventStreamSubscriber.create( new EventStreamHandler<A>() {
                    @Override public void next(A a) {
                        for (B e : f.f( a )) {
                            effect.next( e );
                        }
                    }

                    @Override public void error(Exception e) {
                        effect.error( e );
                    }

                    @Override public void done() {
                        effect.done();
                    }
                } ) );

        return OpenEventStream.wrap( this, open );
    }

}
