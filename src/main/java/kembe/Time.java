package kembe;

import fj.F;
import fj.Ord;
import kembe.sim.rand.RandomGen;
import org.joda.time.*;

import java.util.Random;

public class Time {

    public static final Ord<Instant> instantOrd = Ord.longOrd.comap( new F<Instant, Long>() {
        @Override public Long f(Instant instant) {
            return instant.getMillis();
        }
    } );

    public static F<Interval, Duration> toDuration = new F<Interval, Duration>() {
        @Override public Duration f(Interval interval) {
            return interval.toDuration();
        }
    };

    public static F<Duration, Long> toDurationMillis = new F<Duration, Long>() {
        @Override public Long f(Duration duration) {
            return duration.getMillis();
        }
    };

    public static Instant quantumIncrement(Instant instant) {
        return instant.plus( 1 );
    }

    public static Instant now() {
        return new Instant( System.currentTimeMillis() );
    }

    public static IntervalBuilder from(ReadableInstant instant) {
        return new IntervalBuilder( instant );
    }

    public static RandomGen<Duration> randomDuration(final Duration min, final Duration max) {
       final long diff = Math.abs(max.getMillis() - min.getMillis());

        if(diff>Integer.MAX_VALUE)
            return new RandomGen<Duration>() {
                @Override public Duration next(Random t) {
                    int r = t.nextInt( (int)diff/60/1000 );
                    return min.plus( r * 60 * 1000 );
                }
            };
        else
            return new RandomGen<Duration>() {
                @Override public Duration next(Random t) {
                    int r = t.nextInt( (int)diff );
                    return min.plus( r );
                }
            };
    }

    public static DateTime midnightBefore(ReadableInstant instant) {
        return instant.toInstant().toDateTime().toDateMidnight().toDateTime();
    }

    public static class IntervalBuilder {

        final ReadableInstant start;

        public IntervalBuilder(ReadableInstant start) {
            this.start = start;
        }

        public Interval until(ReadableInstant end) {
            return new Interval( start, end );
        }

        public Interval lasting(ReadableDuration duration) {
            return duration.toDuration().toIntervalFrom( start );
        }

        public Interval lasting(ReadablePeriod period) {
            return new Interval( start, start.toInstant().toDateTime().plus( period ) );
        }


    }

}
