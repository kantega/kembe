package kembe.sim;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.sim.agents.SignalHandlerContext;

import java.util.Random;

public abstract class NonrandomSignalHandler extends SignalHandler {
    @Override public P2<SignalHandler, List<Signal>> signal(Signal signal, Random random,SignalHandlerContext context) {
        P2<SignalHandler, List<Signal>> newState = signal( signal,context );
        return P.p( newState._1(), newState._2() );
    }

    protected abstract P2<SignalHandler, List<Signal>> signal(Signal signal,SignalHandlerContext context);
}
