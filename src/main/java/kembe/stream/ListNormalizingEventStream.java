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

    @Override public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {
        OpenEventStream<List<A>> open =
                source.open(
                        effect.<List<A>>onNext(
                                new Effect<List<A>>() {
                                    @Override
                                    public void e(List<A> next) {
                                        List<A> as = next;
                                        for (A a : as)
                                            effect.e( StreamEvent.next( a ) );
                                    }
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
