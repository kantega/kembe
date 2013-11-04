package kembe.rand;

import fj.F;
import fj.data.List;

import java.util.ArrayList;
import java.util.Random;

public abstract class RandomGen<A> {

    /**
     * Random int in the range[Integer.MIN_VALUE,Integer.MAX_VALUE]
     *
     * @return
     */
    public static RandomGen<AnyInteger> randomInt() {
        return new RandomGen<AnyInteger>() {
            @Override public AnyInteger next(Random t) {
                return new AnyInteger( t.nextInt() );
            }
        };
    }

    /**
     * Random long in the range[Long.MIN_VALUE,Long.MAX_VALUE]
     *
     * @return
     */
    public static RandomGen<AnyLong> randomLong() {
        return new RandomGen<AnyLong>() {
            @Override public AnyLong next(Random t) {
                return new AnyLong( t.nextLong() );
            }
        };
    }

    /**
     * Random double in the range [0,1]
     *
     * @return
     */
    public static RandomGen<DoubleFromZeroIncToOne> randomDouble() {
        return new RandomGen<DoubleFromZeroIncToOne>() {
            @Override public DoubleFromZeroIncToOne next(Random t) {
                return  new DoubleFromZeroIncToOne( t.nextDouble() );
            }
        };
    }

    /**
     * Random double in the range <-infinity,infinity&gt;, gauss distributed with k=0 and std dev = 1
     *
     * @return
     */
    public static RandomGen<AnyDouble> randomGaussDouble() {
        return new RandomGen<AnyDouble>() {
            @Override public AnyDouble next(Random t) {
                return new AnyDouble( t.nextDouble() );
            }
        };
    }

    /**
     * Randomly selects one of the arguments with equal probability
     *
     * @param ts
     * @param <T>
     * @return
     */
    public static <T> RandomGen<T> oneOf(final T... ts) {
        return oneOf( List.list( ts ) );
    }

    /**
     * Randomly selects one of the elements in the list with equal probability
     *
     * @param ts
     * @param <T>
     * @return
     */
    public static <T> RandomGen<T> oneOf(final List<T> ts) {
        final ArrayList<T> copy = new ArrayList(ts.toCollection());

        return randomInt( 0, ts.length() ).map( new F<Integer, T>() {
            @Override public T f(Integer integer) {
                try {
                    T t = copy.get( integer );
                    return t;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Error("COuld not make random T");
                }
            }
        } );
    }

    /**
     * Random int in the range [min,max>
     *
     * @param from
     * @param to
     * @return
     */
    public static RandomGen<Integer> randomInt(final int from, final int to) {

        return new RandomGen<Integer>() {
            @Override public Integer next(Random t) {
                int min = Math.min( from, to );
                int max = Math.max( from, to );
                int diff = max - min;
                return  t.nextInt( diff ) + min;
            }
        };
    }

    public abstract A next(Random t);

    public <B> RandomGen<B> map(final F<A, B> f) {
        return new RandomGen<B>() {
            @Override public B next(Random t) {
                return f.f(RandomGen.this.next( t ));
            }
        };
    }

    public <B> RandomGen<B> bind(final F<A, RandomGen<B>> f) {
        return new RandomGen<B>() {
            @Override public B next(Random t) {
                A r = RandomGen.this.next( t );
                return f.f( r ).next( t );
            }
        };
    }
}
