package kembe.stream;

import fj.Effect;
import fj.P;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

import java.util.ArrayList;

public class JoinEventStream<A, B> extends EventStream<P2<A, List<B>>> {


    private final EventStream<A> one;
    private final EventStream<B> other;

    public JoinEventStream(EventStream<A> one, EventStream<B> other) {
        this.one = one;
        this.other = other;
    }

    @Override
    public OpenEventStream<P2<A, List<B>>> open(final Effect<StreamEvent<P2<A, List<B>>>> effect) {

        final Effect<StreamEvent<Either<A, B>>> eitherE =
                EventStreamSubscriber.create(
                        new Effect<StreamEvent.Next<Either<A, B>>>() {

                            final ArrayList<B> buffer =
                                    new ArrayList<B>();

                            public void e(StreamEvent.Next<Either<A, B>> next) {
                                if (next.value.isLeft()) {
                                    P2<A, List<B>> val = P.p( next.value.left().value(), List.iterableList( buffer ) );
                                    effect.e( StreamEvent.next( val ) );
                                    buffer.clear();
                                } else {
                                    buffer.add( next.value.right().value() );
                                }

                            }
                        },
                        EventStreamSubscriber.<Either<A,B>,P2<A, List<B>>>forwardError( effect ),
                        EventStreamSubscriber.<Either<A,B>,P2<A, List<B>>>forwardDone( effect )
                );

        final OpenEventStream<A> oneO =
                one.open(eitherE.comap(StreamEvent.lift(Either.<A, B>left_())));

        final OpenEventStream<B> otherO =
                other.open(eitherE.comap(StreamEvent.lift(Either.<A, B>right_())));


        return OpenEventStream.wrap(this, List.list(oneO, otherO));
    }
}
