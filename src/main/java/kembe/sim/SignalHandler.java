package kembe.sim;

import fj.P3;
import fj.data.List;

import java.util.Random;

public abstract class SignalHandler {


    public abstract P3<? extends SignalHandler,List<Signal>,Random> signal(Signal signal, Random random);

}
