package kembe.sim;

import fj.P;
import fj.P2;
import fj.P3;
import fj.data.List;

import java.util.Random;

public abstract class NonrandomSignalHandler extends SignalHandler {
    @Override public P3<? extends SignalHandler, List<Signal>, Random> signal(Signal signal, Random random) {
        P2<? extends SignalHandler, List<Signal>> newState = signal( signal );
        return P.p( newState._1(), newState._2(), random );
    }

    protected abstract P2<? extends SignalHandler, List<Signal>> signal(Signal signal);
}
