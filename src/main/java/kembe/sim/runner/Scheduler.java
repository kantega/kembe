package kembe.sim.runner;

import fj.*;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.function.Effect1;
import kembe.sim.Timed;
import kembe.util.Actors;
import kembe.util.Order;
import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Scheduler {

    public static Scheduler realtimeScheduler() {
        return new Scheduler() {

            Timer service = new Timer();

            @Override public void scheduleAt(final DateTime time, final SchedulerTask task) {
                service.schedule( new TimerTask() {
                    @Override public void run() {
                        try {
                            task.run( time );
                        } catch (Exception e) {
                            e.printStackTrace();
                            ;
                        }
                    }
                }, time.toDate() );
            }
        };
    }

    public static Scheduler instantScheduler() {
        return new Scheduler() {

            Actor<Timed<SchedulerTask>> actor = orderedActor( Strategy.<Unit>executorStrategy( Executors.newSingleThreadExecutor() ), Timed.<SchedulerTask>timedOrd(), (Effect1<Timed<SchedulerTask>>) task -> task.value.run( task.time ) );

            @Override public void scheduleAt(DateTime time, SchedulerTask t) {
                actor.act( new Timed<>( time, t ) );
            }
        };
    }

    private static <T> Actor<T> orderedActor(final Strategy<Unit> s, final Ord<T> ord, final Effect1<T> ea) {
        return Actors.stackSafeQueueActor(Strategy.<Unit>seqStrategy(), new Effect1<T>() {

            // Lock to ensure the actor only acts on one message at a time
            AtomicBoolean suspended = new AtomicBoolean(true);

            AtomicLong atomicLong = new AtomicLong(0);

            ConcurrentSkipListSet<Numbered<T>> mbox = new ConcurrentSkipListSet<>(Order.toComparator(numberedOrd(ord)));

            // Product so the actor can use its strategy (to act on messages in other threads,
            // to handle exceptions, etc.)
            P1<Unit> processor = new P1<Unit>() {
                @Override
                public Unit _1() {
                    try {
                        // get next item from queue
                        Numbered<T> a = mbox.pollFirst();
                        // if there is one, process it
                        if (a != null) {
                            ea.f(a.value);
                            // try again, in case there are more messages
                            s.par(this);
                        } else {
                            // clear the lock
                            suspended.set(true);
                            // work again, in case someone else queued up a message while we were holding the lock
                            work();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();//Nothing else to do here
                    }
                    return Unit.unit();
                }
            };

            // Effect's body -- queues up a message and tries to unsuspend the actor
            @Override
            public void f(T a) {
                mbox.add(new Numbered<>(atomicLong.incrementAndGet(), a));
                work();
            }

            // If there are pending messages, use the strategy to run the processor
            protected void work() {
                if (!mbox.isEmpty() && suspended.compareAndSet(true, false)) {
                    s.par(processor);
                }
            }
        });
    }

    private static <T> Ord<Numbered<T>> numberedOrd(Ord<T> ordT) {
        return Ord.p2Ord( ordT, Ord.longOrd ).contramap( new F<Numbered<T>, P2<T, Long>>() {
            @Override public P2<T, Long> f(Numbered<T> tNumbered) {
                return P.p( tNumbered.value, tNumbered.number );
            }
        } );
    }

    public void schedule(Timed<SchedulerTask> timedT) {
        scheduleAt( timedT.time, timedT.value );
    }

    public Effect1<Timed<SchedulerTask>> toEffect() {
        return this::schedule;
    }

    public abstract void scheduleAt(DateTime time, SchedulerTask t);

    static class Numbered<T> {
        public final long number;

        public final T value;

        Numbered(long number, T value) {
            this.number = number;
            this.value = value;
        }
    }
}
