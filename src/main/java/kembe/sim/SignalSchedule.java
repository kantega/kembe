package kembe.sim;

public class SignalSchedule {

    public final RandWait randomSleep;

    public final Signal.SignalF value;

    public SignalSchedule(RandWait randomSleep, Signal.SignalF value) {
        this.randomSleep = randomSleep;
        this.value = value;
    }


}
