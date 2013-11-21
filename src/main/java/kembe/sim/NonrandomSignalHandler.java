package kembe.sim;

import fj.P;
import fj.P2;
import fj.data.Either;
import fj.data.List;

import java.util.Random;

public abstract class NonrandomSignalHandler extends SignalHandler {
    @Override public P2<SignalHandler, List<Signal>> signal(Either<Tick, Signal> signal, Random random, SignalHandlerContext context) {
        P2<SignalHandler, List<Signal>> nextState =
                signal.isLeft() ?
                tick( signal.left().value(), context ) :
                signal( signal.right().value(), context );
        return P.p( nextState._1(), nextState._2() );
    }

    protected abstract P2<SignalHandler, List<Signal>> signal(Signal signal, SignalHandlerContext context);

    protected abstract P2<SignalHandler, List<Signal>> tick(Tick tick, SignalHandlerContext context);
}
