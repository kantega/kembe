package kembe.sim;

import org.joda.time.Instant;

public class Tick {

    public final Instant tickTime;

    public Tick(Instant tickTime) {
        this.tickTime = tickTime;
    }
}
