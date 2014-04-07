package kembe.stream;

import fj.Effect;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EitherEventStream<A, B> extends EventStream<Either<A, B>> {
    private final EventStream<A> one;

    private final EventStream<B> other;

    private final ExecutorService executorService;

    public EitherEventStream(EventStream<A> one, EventStream<B> other) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.one = one;
        this.other = other;

    }

    @Override
    public OpenEventStream<Either<A, B>> open(final EventStreamSubscriber<Either<A, B>> effect) {

        final Actor<StreamEvent<Either<A, B>>> actor =
                Actors.stackSafeQueueActor( Strategy.<Unit>executorStrategy( executorService ), new Effect<StreamEvent<Either<A, B>>>() {
                    @Override public void e(StreamEvent<Either<A, B>> streamEvent) {
                        effect.e( streamEvent );
                    }
                } );

        final OpenEventStream<A> oA = one.open( new EventStreamSubscriber<A>() {
            @Override public void e(StreamEvent<A> event) {
                actor.act( event.map( Either.<A, B>left_() ) );
            }
        } );
        final OpenEventStream<B> oB = other.open( new EventStreamSubscriber<B>() {
            @Override public void e(StreamEvent<B> event) {
                actor.act( event.map( Either.<A, B>right_() ) );
            }
        } );

        return OpenEventStream.wrap( this, List.list( oA, oB ) );
    }
}
