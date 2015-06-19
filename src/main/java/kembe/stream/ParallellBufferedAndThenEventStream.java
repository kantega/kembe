package kembe.stream;

import fj.Effect;
import fj.Unit;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.data.Either;
import fj.data.List;
import fj.function.Effect1;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.util.Actors;
import kembe.util.Effects;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallellBufferedAndThenEventStream<A> extends EventStream<A> {

    private final EventStream<A> first;

    private final EventStream<A> eventual;

    public ParallellBufferedAndThenEventStream(EventStream<A> first, EventStream<A> eventual) {
        this.first = first;
        this.eventual = eventual;
    }

    @Override
    public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {

        final Effect1<Either<StreamEvent<A>, StreamEvent<A>>> bufferingEffect =
                new Effect1<Either<StreamEvent<A>, StreamEvent<A>>>() {

                    final ArrayList<StreamEvent<A>> buffer =
                            new ArrayList<>();

                    final AtomicBoolean flushed =
                            new AtomicBoolean( false );

                    final EventStreamSubscriber<A> eventualHandler =
                            EventStreamSubscriber.create( event -> {
                                if (flushed.get())
                                    effect.e( event );
                                else
                                    buffer.add( event );
                            } );

                    final EventStreamSubscriber<A> firstHandler =
                            effect.onDone(
                                    u -> {

                                        buffer.forEach( effect::e );
                                        flushed.set( true );
                                        buffer.clear();
                                    }
                            );

                    final Actor<Either<StreamEvent<A>, StreamEvent<A>>> actor =
                            Actors.stackSafeQueueActor( Strategy.<Unit>seqStrategy(), event -> {
                                if (event.isLeft()) {
                                    firstHandler.e( event.left().value() );
                                }
                                else {
                                    eventualHandler.e( event.right().value() );
                                }
                            } );

                    @Override
                    public void f(Either<StreamEvent<A>, StreamEvent<A>> event) {
                        actor.act( event );
                    }
                };

        OpenEventStream<A> eventualOpenStream = eventual.open( EventStreamSubscriber.create( Effects.comap(bufferingEffect, Either.<StreamEvent<A>, StreamEvent<A>>right_() ) ) );
        OpenEventStream<A> firstOpenStream = first.open(EventStreamSubscriber.create( Effects.comap( bufferingEffect, Either.<StreamEvent<A>, StreamEvent<A>>left_() ) ) );


        return OpenEventStream.wrap( this, List.<OpenEventStream<?>>list( firstOpenStream, eventualOpenStream ) );
    }
}
