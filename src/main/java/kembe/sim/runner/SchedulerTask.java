package kembe.sim.runner;

import org.joda.time.Instant;

public interface SchedulerTask {

    public void run(Instant time);

}
