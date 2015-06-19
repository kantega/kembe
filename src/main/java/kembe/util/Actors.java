package kembe.util;

import fj.*;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.function.Effect1;

import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Actors {

    public static <T> Actor<T> orderedActor(final Strategy<Unit> s, final Ord<T> ord, final Effect1<T> ea) {
        return Actor.actor( s, new Effect1<T>() {

            // Lock to ensure the actor only acts on one message at a time
            AtomicBoolean suspended = new AtomicBoolean( true );

            ConcurrentSkipListSet<T> mbox = new ConcurrentSkipListSet<>( toComparator( ord ) );

            // Product so the actor can use its strategy (to act on messages in other threads,
            // to handle exceptions, etc.)
            P1<Unit> processor = new P1<Unit>() {
                @Override public Unit _1() {
                    // get next item from queue
                    T a = mbox.pollFirst();
                    // if there is one, process it
                    if (a != null) {
                        ea.f( a );
                        // try again, in case there are more messages
                        s.par( this );
                    }
                    else {
                        // clear the lock
                        suspended.set( true );
                        // work again, in case someone else queued up a message while we were holding the lock
                        work();
                    }
                    return Unit.unit();
                }
            };

            // Effect's body -- queues up a message and tries to unsuspend the actor
            @Override public void f(T a) {
                mbox.add( a );
                work();
            }

            // If there are pending messages, use the strategy to run the processor
            protected void work() {
                if (!mbox.isEmpty() && suspended.compareAndSet( true, false )) {
                    s.par( processor );
                }
            }
        } );
    }

    /**
     * An Actor equipped with a queue and which is guaranteed to process one message at a time.
     * With respect to an enqueueing actor or thread, this actor will process messages in the same order
     * as they are sent.
     */
    public static <T> Actor<T> stackSafeQueueActor(final Strategy<Unit> s, final Effect1<T> ea) {
        return Actor.actor( Strategy.<Unit>seqStrategy(), new Effect1<T>() {

            // Lock to ensure the actor only acts on one message at a time
            AtomicBoolean suspended = new AtomicBoolean( true );

            // Queue to hold pending messages
            ConcurrentLinkedQueue<T> mbox = new ConcurrentLinkedQueue<>();

            // Product so the actor can use its strategy (to act on messages in other threads,
            // to handle exceptions, etc.)
            P1<Unit> processor = new P1<Unit>() {
                @Override public Unit _1() {
                    // get next item from queue
                    try {
                        while (!mbox.isEmpty()) {
                            T a = mbox.poll();
                            ea.f( a );
                        }


                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        // clear the lock
                        suspended.set( true );
                        // work again, in case someone else queued up a message while we were holding the lock
                        work();
                    }
                    return Unit.unit();
                }
            };

            // Effect's body -- queues up a message and tries to unsuspend the actor
            @Override public void f(T a) {
                mbox.offer( a );
                work();
            }

            // If there are pending messages, use the strategy to run the processor
            protected void work() {
                if (!mbox.isEmpty() && suspended.compareAndSet( true, false )) {
                    s.par( processor );
                }
            }
        } );
    }

    ;

    public static <A> Comparator<A> toComparator(final Ord<A> ord) {
        return (o1, o2) -> {
            Ordering o = ord.compare( o1, o2 );
            if (o.equals( Ordering.LT ))
                return -1;
            else if (o.equals( Ordering.EQ ))
                return 0;
            else
                return 1;
        };
    }
}
