package kembe.sim.rand;

import fj.P1;
import fj.data.vector.V;
import fj.data.vector.V2;
import fj.test.Gen;
import fj.test.Property;
import fj.test.reflect.Check;
import fj.test.reflect.CheckParams;
import fj.test.reflect.Name;
import kembe.CheckResults;
import kembe.Time;
import kembe.sim.RandWait;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.Random;

public class RandWaitTest {

    private Random random = new Random( 1 );


    @Name("RandWaitBetween must equally distribute durations in the interval")
    @CheckParams(minSize = 1,minSuccessful = 200)
    Property p1 = Property.property(
            Gen.listOf( Gen.choose( 200, 500000 ).map( Time.intsToDuration )),
            list -> Property.implies( list.length()>4,new P1<Property>() {
                @Override public Property _1() {
                    V2<Integer> count =
                            list.foldLeft( (integers, max) -> {
                                Duration min = new Duration( 10 );
                                DateTime origin = new DateTime( 1 );
                                DateTime time =
                                        RandWait
                                                .waitForBetween( min, max )
                                                .after( origin )
                                                .next( random );

                                Duration half = new Duration( (max.minus( min )).getMillis() / 2 );
                                DateTime lowerBound = origin.plus( min );
                                DateTime upperBound = origin.plus( max );
                                DateTime middle = lowerBound.plus( half );
                                Interval lower = new Interval( lowerBound, middle );
                                Interval higher = new Interval( middle, upperBound );

                                if (lower.contains( time ))
                                    return V.v( integers._1() + 1, integers._2() );
                                else if (higher.contains( time ))
                                    return V.v( integers._1(), integers._2() + 1 );
                                else
                                    return integers;
                            }, V.v( 0, 0 ) );

                    return Property.prop( count._1() > 0 && count._2() > 0 );
                }
            } ) );


    //@Test
    public void testRandWait() {
        CheckResults.assertAndPrintResults(
                Check.check( RandWaitTest.class ) );

    }
}
