package kembe.scheduler;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomTicks {

    public double poissonRandomInterarrivalDelay(int lambda, TimeUnit timeUnit,Random random) {
        return (Math.log(1.0-random.nextDouble())/-timeUnit.toMillis( lambda ));
    }



}
