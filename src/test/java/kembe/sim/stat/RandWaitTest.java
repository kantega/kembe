package kembe.sim.stat;

import fj.test.Gen;
import fj.test.Property;
import fj.test.reflect.Check;
import kembe.CheckResults;
import kembe.Time;
import kembe.sim.RandWait;
import kembe.sim.rand.Rand;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Random;

public class RandWaitTest {

    static final Gen<LocalTime> arbLocalTime =
            Gen.choose( 0, 23 ).bind( Gen.choose( 0, 59 ), hour -> minute -> new LocalTime( hour, minute, 0, 0 ) );

    static final DateTime now =
            new LocalTime( 6, 0 ).toDateTime( Time.now() );

    static final RandWait waitUntilSevenTilNine =
            RandWait.waitUntilBetween( new LocalTime( 7, 0 ), new LocalTime( 9, 0 ) );

    static final DateTime earliest =
            new LocalTime( 7, 0 ).toDateTime( now );

    static final DateTime latest =
            new LocalTime( 9, 0 ).toDateTime( now );

    static final Rand<DateTime> randNext =
            waitUntilSevenTilNine.after( now );

    static final Random random =
            new Random( 5 );

    static final Gen<DateTime> nextBetweenSevenAdnNine =
            Gen.gen( integer -> rand -> randNext.next( random ) );


    Property p2 =
            Property.property( nextBetweenSevenAdnNine, instant -> Property.prop( !instant.isBefore( earliest ) && !instant.isAfter( latest ) ) );

    //@Test
    public void checkProperties() {
        CheckResults.assertAndPrintResults( Check.check( RandWaitTest.class ) );
    }
}
