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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallellBufferedEventStream<A> extends EventStream<A> {

    private final EventStream<A> first;

    private final EventStream<A> eventual;

    public ParallellBufferedEventStream(EventStream<A> first, EventStream<A> eventual) {
        this.first = first;
        this.eventual = eventual;
    }

    @Override
    public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {

        final Effect<Either<StreamEvent<A>, StreamEvent<A>>> bufferingEffect =
                new Effect<Either<StreamEvent<A>, StreamEvent<A>>>() {

                    final ArrayList<A> buffer =
                            new ArrayList<>();

                    final AtomicBoolean flushed =
                            new AtomicBoolean( false );

                    final EventStreamSubscriber<A> eventualHandler =
                            effect.onNext(
                                    new Effect<A>() {
                                        @Override
                                        public void e(A next) {
                                            if (flushed.get())
                                                effect.e( new StreamEvent.Next<>( next ) );
                                            else
                                                buffer.add( next );
                                        }
                                    }
                            );

                    final EventStreamSubscriber<A> firstHandler =
                            effect.onDone(
                                    new Effect<Unit>() {
                                        public void e(Unit u) {

                                            for (A a : buffer) {
                                                effect.e( new StreamEvent.Next<>( a ) );
                                            }
                                            flushed.set( true );
                                            buffer.clear();
                                        }
                                    }
                            );

                    final Actor<Either<StreamEvent<A>, StreamEvent<A>>> actor =
                            Actors.stackSafeQueueActor( Strategy.<Unit>seqStrategy(), new Effect<Either<StreamEvent<A>, StreamEvent<A>>>() {
                                @Override public void e(Either<StreamEvent<A>, StreamEvent<A>> event) {
                                    if (event.isLeft()) {
                                        firstHandler.e( event.left().value() );
                                    }
                                    else {
                                        eventualHandler.e( event.right().value() );
                                    }
                                }
                            } );

                    @Override
                    public void e(Either<StreamEvent<A>, StreamEvent<A>> event) {
                        actor.act( event );
                    }
                };

        OpenEventStream<A> eventualOpenStream = eventual.open( EventStreamSubscriber.create( bufferingEffect.comap( Either.<StreamEvent<A>, StreamEvent<A>>right_() ) ) );
        OpenEventStream<A> firstOpenStream = first.open(EventStreamSubscriber.create( bufferingEffect.comap( Either.<StreamEvent<A>, StreamEvent<A>>left_() ) ) );


        return OpenEventStream.wrap( this, List.<OpenEventStream<?>>list( firstOpenStream, eventualOpenStream ) );
    }
}
