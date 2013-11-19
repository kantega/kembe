package kembe.stream;

import fj.Effect;
import fj.data.List;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class ListNormalizingEventStream<A> extends EventStream<A> {

    private final EventStream<List<A>> source;


    public ListNormalizingEventStream(EventStream<List<A>> source) {
        this.source = source;
    }

    @Override public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {
        OpenEventStream<List<A>> open =
                source.open(
                        EventStreamSubscriber.forwardTo( effect ).<List<A>>onNext(
                                new Effect<StreamEvent.Next<List<A>>>() {
                                    @Override
                                    public void e(StreamEvent.Next<List<A>> next) {
                                        List<A> as = next.value;
                                        for (A a : as)
                                            effect.e( StreamEvent.next( a ) );
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
