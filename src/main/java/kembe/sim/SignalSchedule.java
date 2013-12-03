package kembe.sim;

public class SignalSchedule {

    public final RandWait randomSleep;

    public final Signal value;

    public SignalSchedule(RandWait randomSleep, Signal value) {
        this.randomSleep = randomSleep;
        this.value = value;
    }


}
