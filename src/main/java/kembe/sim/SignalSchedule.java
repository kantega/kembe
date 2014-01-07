package kembe.sim;

import kembe.sim.Signal.SignalBuilder;

public class SignalSchedule {

    public final RandWait randomSleep;

    public final SignalBuilder value;

    public SignalSchedule(RandWait randomSleep, SignalBuilder value) {
        this.randomSleep = randomSleep;
        this.value = value;
    }


}
