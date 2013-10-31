package kembe.sim.stat;

import org.joda.time.Interval;

public abstract class OccurenceProbability {

    public abstract Probability occurenceWithin(Interval interval);

}
