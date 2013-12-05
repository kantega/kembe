package kembe.stream;

import fj.data.Either;
import fj.data.List;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class EitherEventStream<A, B> extends EventStream<Either<A, B>> {
    private final EventStream<A> one;
    private final EventStream<B> other;

    public EitherEventStream(EventStream<A> one, EventStream<B> other) {
        this.one = one;
        this.other = other; }

    @Override
    public OpenEventStream<Either<A, B>> open(final EventStreamSubscriber<Either<A, B>> effect) {
        final OpenEventStream<A> oA = one.open( effect.comapEvent( StreamEvent.lift( Either.<A, B>left_() ) ) );
        final OpenEventStream<B> oB = other.open( effect.comapEvent( StreamEvent.lift( Either.<A, B>right_() ) ) );

        return OpenEventStream.wrap( this, List.list( oA, oB ) );}
}
