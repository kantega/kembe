package kembe.sim.runner;

import org.joda.time.DateTime;

public interface SchedulerTask {

    public void run(DateTime time);

}
