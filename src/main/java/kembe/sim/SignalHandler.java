package kembe.sim;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.sim.agents.SignalHandlerContext;

import java.util.Random;

public abstract class SignalHandler {


    public P2<SignalHandler, List<Signal>> noop() {
        return nextState( this );
    }

    public P2<SignalHandler, List<Signal>> nextState(SignalHandler handler) {
        return P.p( handler, List.<Signal>nil() );
    }

    public SignalHandler self(){
        return this;
    }

    public abstract P2<SignalHandler, List<Signal>> signal(Signal signal, Random random, SignalHandlerContext context);

}
