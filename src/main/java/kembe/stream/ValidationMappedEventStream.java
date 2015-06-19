package kembe.stream;

import fj.Effect;
import fj.Show;
import fj.data.Validation;
import fj.function.Effect1;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;

public class ValidationMappedEventStream<E, T> extends EventStream<T> {
    private final EventStream<Validation<E, T>> wrappedStream;

    private final Show<E> errorShow;

    public ValidationMappedEventStream(EventStream<Validation<E, T>> stream, Show<E> show) {
        this.wrappedStream = stream;
        this.errorShow = show;
    }

    @Override
    public OpenEventStream<T> open(final EventStreamSubscriber<T> effect) {
        OpenEventStream<Validation<E, T>> open =
                wrappedStream.open(
                        effect.onNext(
                                next -> {
                                    if (next.isSuccess())
                                        effect.next( next.success() );
                                    else
                                        effect.error( new Exception( errorShow.showS( next.fail() ) ) );
                                } ) );

        return OpenEventStream.wrap( this, open );
    }
}
