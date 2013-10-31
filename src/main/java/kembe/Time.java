package kembe;

import fj.F;
import fj.Ord;
import org.joda.time.*;

public class Time {

    public static final Ord<Instant> instantOrd = Ord.longOrd.comap( new F<Instant, Long>() {
        @Override public Long f(Instant instant) {
            return instant.getMillis();
        }
    } );

    public static Instant quantumIncrement(Instant instant){
        return instant.plus( 1 );
    }

    public static Instant now(){
        return new Instant( System.currentTimeMillis() );
    }
    public static F<Interval,Duration> toDuration = new F<Interval, Duration>() {
        @Override public Duration f(Interval interval) {
            return interval.toDuration();
        }
    };

    public static F<Duration,Long> toDurationMillis = new F<Duration, Long>() {
        @Override public Long f(Duration duration) {
            return duration.getMillis();
        }
    };

    public static IntervalBuilder from(ReadableInstant instant){
        return new IntervalBuilder( instant );
    }


    public static DateTime midnightBefore(ReadableInstant instant){
        return instant.toInstant().toDateTime().toDateMidnight().toDateTime();
    }

    public static class IntervalBuilder{

        final ReadableInstant start;

        public IntervalBuilder(ReadableInstant start) {
            this.start = start;
        }

        public Interval until(ReadableInstant end){
            return new Interval(start,end);
        }

        public Interval lasting(ReadableDuration duration){
            return duration.toDuration().toIntervalFrom( start );
        }

        public Interval lasting(ReadablePeriod period){
            return new Interval(start,start.toInstant().toDateTime().plus( period ));
        }
    }

}
