package kembe.sim.stat;

import org.joda.time.Interval;
import org.joda.time.LocalTime;

public abstract class OccurenceProbability {

    public static OccurenceProbability wholeDay(Probability p){
        return new LocalTimeRangeOP( new LocalTime( 0,0 ),new LocalTime( 23,59,59,999 ),p );
    }

    public abstract Probability occurenceWithin(Interval interval);

}
