package kembe.sim.stat;

import fj.data.Option;
import kembe.Time;
import kembe.rand.DoubleFromZeroIncToOne;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

public class LocalTimeRangeOP extends OccurenceProbability {
    public final LocalTime from;

    public final LocalTime to;

    public final Probability probability;

    public LocalTimeRangeOP(LocalTime from, LocalTime to, Probability probability) {
        this.from = from.isBefore( to ) ? from : to;
        this.to = to.isAfter( from ) ? to : from;
        this.probability = probability;

    }

    @Override public Probability occurenceWithin(Interval test) {
        if (test.toDurationMillis() == 0)
            return new Probability( new DoubleFromZeroIncToOne( 0 ) );


        DateTime actualStart = test.getStart().withFields( from );
        DateTime actualEnd = test.getStart().withFields( to );

        //Realize the interval of the localtimes
        Interval firstInterval = new Interval( actualStart, actualEnd );
        Duration localDuration = firstInterval.toDuration();

        Option<Duration> startOverlap =
                Option.fromNull( firstInterval
                        .overlap( test ) )
                        .map( Time.toDuration );

        //Make the test again with the end date, so we dont miss intervals that contain midnight
        DateTime endStart = test.getEnd().withFields( from );
        DateTime endEnd = test.getEnd().withFields( to );
        Option<Duration> endOverlap =
                Option.fromNull( new Interval( endStart, endEnd )
                        .overlap( test ) )
                        .map( Time.toDuration );


        long overlapMillis = (startOverlap.orElse( endOverlap ).map( Time.toDurationMillis ).orSome( 0L ));
        double ratio = ((double)overlapMillis) / localDuration.getMillis();

        return new Probability(
                new DoubleFromZeroIncToOne(
                        ratio * probability.threshold.value ) );
    }

    @Override public String toString() {
        return "LocalTimeRangeOP(" +
                "from=" + from +
                ", to=" + to +
                ", probability=" + probability.threshold.value +
                ')';
    }
}
