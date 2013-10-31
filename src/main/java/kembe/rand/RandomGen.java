package kembe.rand;

import fj.F;
import fj.P;
import fj.P2;

import java.util.Random;

public abstract class RandomGen<A> {

    /**
     * Random int in the range[Integer.MIN_VALUE,Integer.MAX_VALUE]
     * @return
     */
    public static RandomGen<AnyInteger> randomInt() {
        return new RandomGen<AnyInteger>() {
            @Override public P2<AnyInteger, Random> next(Random t) {
                return P.p( new AnyInteger(t.nextInt()), new Random( t.nextLong() ) );
            }
        };
    }

    /**
     * Random double in the range [0,1]
     * @return
     */
    public static RandomGen<DoubleFromZeroIncToOne> randomDouble(){
        return new RandomGen<DoubleFromZeroIncToOne>() {
            @Override public P2<DoubleFromZeroIncToOne, Random> next(Random t) {
                return P.p( new DoubleFromZeroIncToOne( t.nextDouble()) , new Random( t.nextLong() ) );
            }
        };
    }

    /**
     * Random double in the range <-infinity,infinity&gt;, gauss distributed with k=0 and std dev = 1
     * @return
     */
    public static RandomGen<AnyDouble> randomGaussDouble(){
        return new RandomGen<AnyDouble>() {
            @Override public P2<AnyDouble, Random> next(Random t) {
                return P.p( new AnyDouble( t.nextDouble()) , new Random( t.nextLong() ) );
            }
        };
    }

    /**
     * Random int in the range [min,max>
     * @param from
     * @param to
     * @return
     */
    public static RandomGen<Integer> randomInt(final int from, final int to) {

        return new RandomGen<Integer>() {
            @Override public P2<Integer, Random> next(Random t) {
                int min = Math.min( from, to );
                int max = Math.max( from, to );
                int diff = max - min;
                return P.p( t.nextInt( diff ) + min, new Random( Math.abs( t.nextLong() ) ) );
            }
        };
    }


    public abstract P2<A, Random> next(Random t);

    public <B> RandomGen<B> map(final F<A,B> f){
        return new RandomGen<B>() {
            @Override public P2<B, Random> next(Random t) {
                return RandomGen.this.next( t ).map1( f );
            }
        };
    }

    public <B> RandomGen<B> bind(final F<A,RandomGen<B>> f){
        return new RandomGen<B>() {
            @Override public P2<B, Random> next(Random t) {
                P2<A,Random> r =  RandomGen.this.next( t );
                return f.f(r._1()).next( r._2() );
            }
        };
    }
}
