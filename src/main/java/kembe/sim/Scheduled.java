package kembe.sim;

import fj.*;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.data.Either;
import fj.data.Option;
import org.joda.time.Instant;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static fj.control.parallel.Actor.queueActor;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * Represents a non-blocking future value. Products, functions, and actors, given to the methods on this class,
 * are executed concurrently, and the Promise serves as a handle on the result of the computation. Provides monadic
 * operations so that future computations can be combined
 * <p/>
 * Author: Runar
 */
public final class Scheduled<A> {

    private final Actor<P2<Either<A, Actor<A>>, Scheduled<A>>> actor;
    private final Strategy<Unit> s;
    private final CountDownLatch l = new CountDownLatch( 1 );
    private final Queue<Actor<A>> waiting = new LinkedList<Actor<A>>();
    private volatile Option<A> v = none();

    private Scheduled(final Strategy<Unit> s, final Actor<P2<Either<A, Actor<A>>, Scheduled<A>>> qa) {
        this.s = s;
        actor = qa;
    }

    private static <A> Scheduled<A> mkScheduled(final Strategy<Unit> s) {
        final Actor<P2<Either<A, Actor<A>>, Scheduled<A>>> q =
                queueActor( s, new Effect<P2<Either<A, Actor<A>>, Scheduled<A>>>() {
                    public void e(final P2<Either<A, Actor<A>>, Scheduled<A>> p) {
                        final Scheduled<A> snd = p._2();
                        final Queue<Actor<A>> as = snd.waiting;
                        if (p._1().isLeft()) {
                            final A a = p._1().left().value();
                            snd.v = some( a );
                            snd.l.countDown();
                            while (!as.isEmpty())
                                as.remove().act( a );
                        } else if (snd.v.isNone())
                            as.add( p._1().right().value() );
                        else
                            p._1().right().value().act( snd.v.some() );
                    }
                } );
        return new Scheduled<A>( s, q );
    }

    public static <A> Scheduled<A> scheduleAt(Strategy<Unit> strategy, Timer timer, Instant instant, final Callable<A> callable) {
        final Scheduled<A> s = mkScheduled( strategy );
        timer.schedule( new TimerTask() {
            @Override public void run() {
                try {
                    s.actor.act( P.p( Either.<A,Actor<A>>left( callable.call() ), s ) );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, instant.toDate() );
        return s;
    }

    /**
     */
    public static <A> Scheduled<A> scheduled(final Strategy<Unit> s, final A a) {
        final Scheduled<A> p = mkScheduled( s );
        p.actor.act( P.p( Either.<A, Actor<A>>left( a ), p ) );
        return p;
    }

    /**
     */
    public static <A> F<A, Scheduled<A>> scheduled(final Strategy<Unit> s) {
        return new F<A, Scheduled<A>>() {
            public Scheduled<A> f(final A a) {
                return scheduled( s, a );
            }
        };
    }

    /**
     */
    public static <A, B> F<A, Scheduled<B>> scheduled(final Strategy<Unit> s, final F<A, B> f) {
        return new F<A, Scheduled<B>>() {
            public Scheduled<B> f(final A a) {
                return scheduled( s, f.f( a ) );
            }
        };
    }

    /**
     */
    public void to(final Actor<A> a) {
        actor.act( P.p( Either.<A, Actor<A>>right( a ), this ) );
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @return The promised value.
     */
    public A claim() {
        try {
            l.await();
        } catch (InterruptedException e) {
            throw new Error( e );
        }
        return v.some();
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return The promised value, or none if the timeout was reached.
     */
    public Option<A> claim(final long timeout, final TimeUnit unit) {
        try {
            if (l.await( timeout, unit ))
                return v;
        } catch (InterruptedException e) {
            throw new Error( e );
        }
        return none();
    }

    /**
     * Returns true if this promise has been fulfilled.
     *
     * @return true if this promise has been fulfilled.
     */
    public boolean isFulfilled() {
        return v.isSome();
    }


}