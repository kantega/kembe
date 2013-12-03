package kembe.sim.stat;

import fj.Monoid;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.NonEmptyList;
import kembe.sim.rand.Rand;
import org.apache.commons.lang3.Range;

import java.util.Random;

public class Draw<A> extends Rand<A> {

    private final NonEmptyList<P2<Double, A>> alternatives;

    protected Draw(NonEmptyList<P2<Double, A>> alternatives) {
        this.alternatives = alternatives;
    }

    public static <A> Rand<A> only(A a) {
        return new Draw<>( NonEmptyList.nel( P.p( 1d, a ) ) );
    }

    public static <A> Draw<A> alt(double weight, A a) {
        return new Draw<>( NonEmptyList.nel( P.p( weight, a ) ) );
    }

    public static <A> Draw<A> alt(int weight, A a) {
        return new Draw<>( NonEmptyList.nel( P.p( (double) weight, a ) ) );
    }

    public Draw<A> or(double weight, A a) {
        return new Draw<>( alternatives.cons( P.p( weight, a ) ) );
    }

    public Draw<A> or(int weight, A a) {
        return new Draw<>( alternatives.cons( P.p( (double) weight, a ) ) );
    }

    @Override public A next(Random random) {
        List<Double> ratios = alternatives.toList().map( P2.<Double, A>__1() );
        Double sum = Monoid.doubleAdditionMonoid.sumLeft( ratios );
        Double randomNumber = Rand.randomDouble().next( random ).value * sum;
        return within( 0d, randomNumber, alternatives.toList() );
    }


    private A within(Double lowerBound, Double randomNumber, List<P2<Double, A>> list) {

        P2<Double, A> head = list.head();
        Double upperBound = lowerBound + head._1();

        if (Range.between( lowerBound, upperBound ).contains( randomNumber ))
            return head._2();

        else
            return within( upperBound, randomNumber, list.tail() );
    }


}
