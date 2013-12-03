package kembe.sim;

import fj.F;
import kembe.sim.rand.DoubleFromZeroIncToOne;
import kembe.sim.rand.Rand;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadablePeriod;

import java.util.Random;

public class Occurring<T> {

    public final Rand<Instant> randomTime;

    public final T value;

    public Occurring(Rand<Instant> randomTime, T value) {
        this.randomTime = randomTime;
        this.value = value;
    }

    public static Rand<Instant> at(final Instant instant) {
        return new Rand<Instant>() {
            @Override public Instant next(Random random) {
                return instant;
            }
        };
    }

    public static Rand<Instant> within(final Interval interval) {
        Rand<Instant> rg = new Rand<Instant>() {

            @Override public Instant next(Random random) {
                return Rand.randomDouble().map( new F<DoubleFromZeroIncToOne, Instant>() {
                    @Override
                    public Instant f(DoubleFromZeroIncToOne d) {
                        long fraction = (long) (interval.toDurationMillis() * d.value);
                        return new Instant( interval.getStartMillis() + fraction );
                    }
                } ).next( random );
            }
        };

        return rg;
    }

    public static Rand<Instant> withinMillis(Instant instant, long millis) {
        return within( new Interval( instant, instant.plus( millis ) ) );
    }

    public static Rand<Instant> within(Instant instant, ReadableDuration d) {
        return within( new Interval( instant, instant.plus( d ) ) );
    }

    public static Rand<Instant> within(Instant instant, ReadablePeriod p) {
        return within( new Interval( instant, instant.plus( p.toPeriod().toStandardDuration() ) ) );
    }
}
