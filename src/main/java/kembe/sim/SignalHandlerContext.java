package kembe.sim;

import kembe.sim.ResourceId;
import org.joda.time.Instant;
import org.joda.time.Interval;

public class SignalHandlerContext {
    public final ResourceId id;

    public final Instant lastExecutionTime;

    public final Instant currentTime;

    public final Interval intervalSinceLast;

    public SignalHandlerContext(ResourceId id, Instant lastExecutionTime, Instant currentTime, Interval intervalSinceLast) {
        this.id = id;
        this.lastExecutionTime = lastExecutionTime;
        this.currentTime = currentTime;
        this.intervalSinceLast = intervalSinceLast;
    }
}
