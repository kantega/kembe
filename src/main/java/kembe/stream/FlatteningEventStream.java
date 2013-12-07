package kembe.stream;

import fj.Unit;
import kembe.*;
import kembe.util.Effects;

public class FlatteningEventStream<A> extends EventStream<A> {

    private final EventStream<EventStream<A>> wrapped;

    public FlatteningEventStream(EventStream<EventStream<A>> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {
        OpenEventStream<EventStream<A>> a =
                wrapped.open(
                        EventStreamSubscriber.create( new EventStreamHandler<EventStream<A>>() {
                            @Override
                            public void next(EventStream<A> as) {
                                as.open( effect.onDone( Effects.<Unit>noOp() ) );
                            }

                            @Override
                            public void error(Exception e) {
                                effect.e( StreamEvent.<A>error( e ) );
                            }

                            @Override
                            public void done() {
                                effect.e( StreamEvent.<A>done() );
                            }
                        } ) );

        return OpenEventStream.wrap( this, a );
    }
}
