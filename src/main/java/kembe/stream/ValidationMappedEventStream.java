package kembe.stream;

import fj.Effect;
import fj.Show;
import fj.data.Validation;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class ValidationMappedEventStream<E, T> extends EventStream<T> {
    private final EventStream<Validation<E, T>> wrappedStream;

    private final Show<E> errorShow;

    public ValidationMappedEventStream(EventStream<Validation<E, T>> stream, Show<E> show) {
        this.wrappedStream = stream;
        this.errorShow = show;
    }

    @Override
    public OpenEventStream<T> open(final Effect<StreamEvent<T>> effect) {
        OpenEventStream<Validation<E, T>> open =
                wrappedStream.open(
                        EventStreamSubscriber.forwardTo( effect ).onNext(
                                new Effect<StreamEvent.Next<Validation<E, T>>>() {
                                    @Override
                                    public void e(StreamEvent.Next<Validation<E, T>> next) {
                                        if (next.value.isSuccess())
                                            effect.e( StreamEvent.next( next.value.success() ) );
                                        else
                                            effect.e( StreamEvent.<T>error( new Exception( errorShow.showS( next.value.fail() ) ) ) );
                                    }

                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
