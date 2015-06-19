package kembe.sim.rand;

import fj.F;
import fj.data.List;

import java.util.ArrayList;
import java.util.Random;

public abstract class Rand<A> {

    /**
     * Random int in the range[Integer.MIN_VALUE,Integer.MAX_VALUE]
     *
     * @return A random generator for any integer
     */
    public static Rand<AnyInteger> randomInt() {
        return new Rand<AnyInteger>() {
            @Override public AnyInteger next(Random t) {
                return new AnyInteger( t.nextInt() );
            }
        };
    }

    /**
     * Random long in the range[Long.MIN_VALUE,Long.MAX_VALUE]
     *
     * @return a random generator for long values
     */
    public static Rand<AnyLong> randomLong() {
        return new Rand<AnyLong>() {
            @Override public AnyLong next(Random t) {
                return new AnyLong( t.nextLong() );
            }
        };
    }

    /**
     * Random double in the range [0,1]
     *
     * @return a generator of doubles between 0 and 1
     */
    public static Rand<DoubleFromZeroIncToOne> randomDouble() {
        return new Rand<DoubleFromZeroIncToOne>() {
            @Override public DoubleFromZeroIncToOne next(Random t) {
                return  new DoubleFromZeroIncToOne( t.nextDouble() );
            }
        };
    }

    /**
     * Random double in the range <-infinity,infinity&gt;, gauss distributed with k=0 and std dev = 1
     *
     * @return a random generator
     */
    public static Rand<AnyDouble> randomGaussDouble() {
        return new Rand<AnyDouble>() {
            @Override public AnyDouble next(Random t) {
                return new AnyDouble( t.nextDouble() );
            }
        };
    }

    /**
     * Randomly selects one of the arguments with equal probability
     *
     * @param ts the posibilities
     * @param <T> the type of values
     * @return a generator
     */
    public static <T> Rand<T> oneOf(final T... ts) {
        return oneOf( List.list( ts ) );
    }

    /**
     * Randomly selects one of the elements in the list with equal probability
     *
     * @param ts the posibilities
     * @param <T> the type of values
     * @return a generator
     */
    public static <T> Rand<T> oneOf(final List<T> ts) {
        final ArrayList<T> copy = new ArrayList(ts.toCollection());

        return randomInt( 0, ts.length() ).map( integer -> {
            try {
                T t = copy.get( integer );
                return t;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error("COuld not make random T");
            }
        } );
    }

    /**
     * Random int in the range [min,max>
     *
     * @param from minvalue
     * @param to maxvalue
     * @return a generator
     */
    public static Rand<Integer> randomInt(final int from, final int to) {

        return new Rand<Integer>() {
            @Override public Integer next(Random t) {
                int min = Math.min( from, to );
                int max = Math.max( from, to );
                int diff = max - min;
                return  t.nextInt( diff ) + min;
            }
        };
    }

    public abstract A next(Random t);

    public <B> Rand<B> map(final F<A, B> f) {
        return new Rand<B>() {
            @Override public B next(Random t) {
                return f.f(Rand.this.next( t ));
            }
        };
    }

    public <B> Rand<B> bind(final F<A, Rand<B>> f) {
        return new Rand<B>() {
            @Override public B next(Random t) {
                A r = Rand.this.next( t );
                return f.f( r ).next( t );
            }
        };
    }
}
