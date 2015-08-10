package kembe.stream;

import fj.Unit;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.data.Either;
import fj.data.List;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.util.Actors;

public class EitherEventStream<A, B> extends EventStream<Either<A, B>> {

    private final EventStream<A> one;

    private final EventStream<B> other;


    public EitherEventStream(EventStream<A> one, EventStream<B> other) {
        this.one = one;
        this.other = other;

    }

    @Override
    public OpenEventStream<Either<A, B>> open(final EventStreamSubscriber<Either<A, B>> effect) {

        Actor<StreamEvent<Either<A, B>>> sync =
                Actors.stackSafeQueueActor( Strategy.<Unit>seqStrategy(), effect::e );


        final OpenEventStream<A> oA = one.open( new EventStreamSubscriber<A>() {
            @Override public void e(StreamEvent<A> event) {
                sync.act( event.map( Either.<A, B>left_() ) );
            }
        } );
        final OpenEventStream<B> oB = other.open( new EventStreamSubscriber<B>() {
            @Override public void e(StreamEvent<B> event) {
                sync.act( event.map( Either.<A, B>right_() ) );
            }
        } );

        return OpenEventStream.wrap( this, List.list( oA, oB ) );
    }
}
