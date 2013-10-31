package kembe.sim.stat;

import fj.Equal;
import fj.F;
import fj.Ord;
import kembe.rand.DoubleFromZeroIncToOne;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Probability {

    public static final Probability one = new Probability( DoubleFromZeroIncToOne.one );
    public static final Probability zero = new Probability( DoubleFromZeroIncToOne.zero );
    public static final Probability half = new Probability( DoubleFromZeroIncToOne.half );
    public static final Probability ninetyPercent = new Probability( new DoubleFromZeroIncToOne(0.900) );
    public static final Probability ninetyninePercent = new Probability( new DoubleFromZeroIncToOne(0.990) );
    public static final Probability tenPercent = new Probability( new DoubleFromZeroIncToOne(0.100) );
    public static final Probability onePercent = new Probability( new DoubleFromZeroIncToOne(0.010) );

    public static Ord<Probability> ord = Ord.doubleOrd.comap( new F<Probability, Double>() {
        @Override public Double f(Probability probability) {
            return probability.threshold.value;
        }
    } );

    public static Equal<Probability> roundedEq = Equal.bigdecimalEqual.comap( new F<Probability, BigDecimal>() {
        @Override public BigDecimal f(Probability probability) {
            return new BigDecimal( probability.threshold.value ).setScale( 8,RoundingMode.DOWN );
        }
    } );

    public final DoubleFromZeroIncToOne threshold;

    public Probability(DoubleFromZeroIncToOne threshold) {
        this.threshold = threshold;
    }

    public static Probability plus(Probability one, Probability other) {
        return new Probability( new DoubleFromZeroIncToOne( one.threshold.value + ((1.0 - one.threshold.value) * other.threshold.value) ) );
    }

    @Override public String toString() {
        return "Probability(" +
                "threshold=" + threshold.value +
                ')';
    }
}
