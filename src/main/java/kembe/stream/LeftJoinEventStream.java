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

public class LeftJoinEventStream<A, B> extends EventStream<P2<A, List<B>>> {


    private final EventStream<Either<A,B>> source;

    public LeftJoinEventStream(EventStream<Either<A,B>> source) {
        this.source = source;
    }

    @Override
    public OpenEventStream<P2<A, List<B>>> open(final Effect<StreamEvent<P2<A, List<B>>>> effect) {

        OpenEventStream<Either<A,B>> abs = source.open(
                EventStreamSubscriber.forwardTo(effect).onNext(
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
                        }));




        return OpenEventStream.wrap(this, abs);
    }
}
