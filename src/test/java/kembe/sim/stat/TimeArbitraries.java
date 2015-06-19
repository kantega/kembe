package kembe.sim.stat;

import fj.F2;
import fj.Function;
import fj.test.Arbitrary;
import fj.test.Gen;
import kembe.sim.rand.DoubleFromZeroIncToOne;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

public class TimeArbitraries {
    public static final Arbitrary<LocalTime> arbLocalTime =
            Arbitrary.arbitrary( Gen.choose( 0, 23 ).bind( Gen.choose( 0, 59 ), Function.curry( (F2<Integer, Integer, LocalTime>) LocalTime::new ) ) );

    public static final Arbitrary<Probability> arbProbability = Arbitrary.arbitrary( Gen.choose( 0, 1000000 ).map( d -> new Probability( new DoubleFromZeroIncToOne( ((double) d) / 1000000 ) ) ) );


    public static Arbitrary<Duration> arbDuration(final Duration min, final Duration max) {
        if (max.getMillis() < Integer.MAX_VALUE - 1) {
            Gen<Integer> durInMillis = Gen.choose( (int) min.getMillis(), (int) max.getMillis() );
            return Arbitrary.arbitrary( durInMillis.map( Duration::new ) );
        }
        else {
            Gen<Integer> durInSeconds = Gen.choose( (int) min.getStandardSeconds(), (int) max.getStandardSeconds() );
            return Arbitrary.arbitrary( durInSeconds.map( integer -> new Duration( integer * 1000L ) ) );
        }
    }

    public static final Arbitrary<Instant> arbInstant(Interval interval) {
        Gen<Instant> instantGen =
                Gen.choose( (int) (interval.getStart().getMillis() / 1000 / 60), (int) (interval.getEnd().getMillis() / 1000 / 60) )
                        .map( integer -> new Instant( integer * 1000L * 60 ) );

        return Arbitrary.arbitrary( instantGen );
    }

    public static final Arbitrary<Interval> arbInterval(Interval startInterval, Duration min, Duration max) {
        Gen<Interval> intervalGen = arbInstant( startInterval ).gen.bind( arbDuration( min, max ).gen, Function.curry( ((F2<Instant, Duration, Interval>) (instant, duration) -> new Interval( instant, instant.plus( duration ) )) ) );
        return Arbitrary.arbitrary( intervalGen );
    }
}
