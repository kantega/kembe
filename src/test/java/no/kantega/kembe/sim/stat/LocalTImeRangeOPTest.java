package no.kantega.kembe.sim.stat;

import fj.F;
import fj.Ordering;
import fj.P2;
import fj.test.Arbitrary;
import fj.test.Property;
import fj.test.reflect.Check;
import fj.test.reflect.Name;
import kembe.rand.DoubleFromZeroIncToOne;
import kembe.sim.stat.LocalTimeRangeOP;
import kembe.sim.stat.Probability;
import no.kantega.kembe.CheckResults;
import org.joda.time.*;
import org.junit.Test;

import static kembe.Time.*;

public class LocalTimeRangeOPTest {
    static int counter = 0;

    @Name("Full overlap must yield the full probability")
    Property p1 = Property.property( TimeArbitraries.arbTimeRangeOP, new F<LocalTimeRangeOP, Property>() {
        @Override public Property f(LocalTimeRangeOP localTimeRangeOP) {

            DateMidnight dm = now().toDateTime().toDateMidnight();
            Interval day = new Interval( dm, dm.plusDays( 1 ) );

            Probability p = localTimeRangeOP.occurenceWithin( day );
            return Property.prop( p.threshold.value == localTimeRangeOP.probability.threshold.value );
        }
    } );

    @Name("Probability addition is non-associative")
    Property p2 = Property.property( Arbitrary.arbP2( TimeArbitraries.arbProbability, TimeArbitraries.arbProbability ), new F<P2<Probability, Probability>, Property>() {
        @Override public Property f(P2<Probability, Probability> ps) {
            Probability sum1 = Probability.plus( ps._1(), ps._2() );
            Probability sum2 = Probability.plus( ps._2(), ps._1() );
            counter++;
            if (counter % 1000 == 0)
                System.out.println( counter );

            return Property.prop( Probability.roundedEq.eq( sum1, sum2 ) );
        }
    } );

    @Name("The probability is the given probability times the relative time between the localtimespan and the overlapping timespan")
    Property p3 = Property.property( TimeArbitraries.arbInstant( from( midnightBefore( now() ) ).until( midnightBefore( now() ).plus( Hours.SIX ) ) ), new F<Instant, Property>() {
        @Override public Property f(Instant instant) {
            Interval interval = from( instant ).lasting( Hours.ONE );
            LocalTimeRangeOP trOP = new LocalTimeRangeOP( new LocalTime( 2, 0 ), new LocalTime( 4, 0 ), Probability.half );
            Probability p = trOP.occurenceWithin( interval );

            Ordering comparedToHalf = Probability.ord.compare( p , new Probability( new DoubleFromZeroIncToOne( 0.25 ) ) );

            return Property.prop( (comparedToHalf.equals( Ordering.LT ) || comparedToHalf.equals( Ordering.EQ ))  );
        }
    } );

    @Test
    public void checkProbabilities() {
        CheckResults.assertAndPrintResults(
                Check.check( LocalTimeRangeOPTest.class ) );
    }
}
