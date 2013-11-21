package kembe.sim;

import fj.P;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import kembe.sim.rand.RandomGen;

import java.util.Random;

public abstract class RandomSignalHandlerAdapter<R> extends SignalHandler {

    private final RandomGen<R> randomGen;

    protected RandomSignalHandlerAdapter(RandomGen<R> randomGen) {
        this.randomGen = randomGen;
    }

    @Override public P2<SignalHandler, List<Signal>> signal(Either<Tick, Signal> signal, Random random, SignalHandlerContext context) {
        R nextRandom = randomGen.next( random );
        P2<SignalHandler, List<Signal>> nextState =
                signal.isLeft() ?
                tick( signal.left().value(), nextRandom ,context) :
                signalRandom( signal.right().value(),nextRandom,context );
        return P.p( nextState._1(), nextState._2() );
    }

    protected abstract P2<SignalHandler, List<Signal>> signalRandom(Signal signal, R randomValue, SignalHandlerContext context);

    protected abstract P2<SignalHandler, List<Signal>> tick(Tick tick, R randomValue, SignalHandlerContext context);
}
