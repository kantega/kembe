package kembe;

import fj.Unit;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.function.Effect1;
import kembe.util.Actors;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus<A> {

    private final CopyOnWriteArrayList<EventStreamSubscriber<A>> observers = new CopyOnWriteArrayList<>();

    private final Actor<StreamEvent<A>> actor;

    public EventBus() {
        actor = Actors.stackSafeQueueActor(Strategy.<Unit>seqStrategy(), a -> {
                    for (EventStreamSubscriber<A> observer : observers) {
                        observer.e(a);
                    }
                }
        );
    }

    public EventStream<A> stream() {
        return new EventStream<A>() {
            @Override
            public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {
                observers.add(effect);
                final EventStream<A> self = this;
                return new OpenEventStream<A>() {
                    @Override
                    public EventStream<A> close() {
                        observers.remove(effect);
                        return self;
                    }
                };
            }
        };
    }

    public EventStreamSubscriber<A> subscriber() {
        return EventStreamSubscriber.create(toEffect());
    }

    public void next(A a) {
        submit(StreamEvent.next(a));
    }

    public void error(Exception e) {
        submit(StreamEvent.<A>error(e));
    }

    public void done() {
        submit(StreamEvent.<A>done());
    }

    public Effect1<StreamEvent<A>> toEffect() {
        return this::submit;
    }

    private void submit(StreamEvent<A> event) {
        actor.act(event);
    }
}
