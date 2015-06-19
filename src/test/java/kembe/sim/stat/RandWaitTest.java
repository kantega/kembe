package kembe.sim.stat;

import fj.F;
import fj.test.Arbitrary;
import fj.test.Gen;
import fj.test.Property;
import fj.test.reflect.Check;
import kembe.CheckResults;
import kembe.Time;
import kembe.sim.RandWait;
import kembe.sim.rand.Rand;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.Random;

public class RandWaitTest {

    static final Arbitrary<LocalTime> arbLocalTime = Arbitrary.arbitrary( Gen.choose( 0, 23 ).bind( Gen.choose( 0, 59 ), new F<Integer, F<Integer, LocalTime>>() {
        public F<Integer, LocalTime> f(final Integer hour) {
            return new F<Integer, LocalTime>() {
                public LocalTime f(final Integer minute) {
                    return new LocalTime( hour, minute, 0, 0 );
                }
            };
        }
    } ) );

    static final DateTime now = new LocalTime( 6, 0 ).toDateTime( Time.now() );

    static final RandWait waitUntilSevenTilNine = RandWait.waitUntilBetween( new LocalTime( 7, 0 ), new LocalTime( 9, 0 ) );

    static final DateTime earliest = new LocalTime( 7, 0 ).toDateTime( now );

    static final DateTime latest = new LocalTime( 9, 0 ).toDateTime( now );

    static final Rand<DateTime> randNext = waitUntilSevenTilNine.after( now );

    static final Random random = new Random( 5 );

    static final Arbitrary<DateTime> nextBetweenSevenAdnNine = Arbitrary.arbitrary( Gen.gen( new F<Integer, F<fj.test.Rand, DateTime>>() {
        public F<fj.test.Rand, DateTime> f(final Integer integer) {
            return new F<fj.test.Rand, DateTime>() {
                public DateTime f(final fj.test.Rand rand) {
                    return randNext.next( random );
                }
            };
        }
    } ) );


    Property p2 = Property.property( nextBetweenSevenAdnNine, new F<DateTime, Property>() {
        public Property f(final DateTime instant) {

            return Property.prop( !instant.isBefore( earliest ) && !instant.isAfter( latest ) );
        }
    } );

    //@Test
    public void checkProperties() {
        CheckResults.assertAndPrintResults( Check.check( RandWaitTest.class ) );
    }
}
