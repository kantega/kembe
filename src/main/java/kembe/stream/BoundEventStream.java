package kembe.stream;

import fj.Effect;
import fj.F;
import kembe.EventStream;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;

public class BoundEventStream<A, B> extends EventStream<B>
{
    private final F<A, EventStream<B>> f;

    private final EventStream<A> bound;

    public BoundEventStream(EventStream<A> bound, F<A, EventStream<B>> f)
    {
        this.f = f;
        this.bound = bound;
    }

    @Override
    public OpenEventStream<B> open(final Effect<StreamEvent<B>> effect)
    {
        OpenEventStream<A> a =
                bound.open(
                        EventStreamSubscriber.create(new EventStreamHandler<A>()
                        {
                            @Override
                            public void next(A a)
                            {
                                EventStream<B> bs = f.f(a);
                                bs.open(
                                        EventStreamSubscriber.forwardTo(effect).onDone(
                                                EventStreamSubscriber.<B>noOpDone()
                                        )
                                );
                            }

                            @Override
                            public void error(Exception e)
                            {
                                effect.e(StreamEvent.<B>error(e));
                            }

                            @Override
                            public void done()
                            {
                                effect.e(StreamEvent.<B>done());
                            }
                        }));

        return OpenEventStream.wrap(this, a);
    }
}
