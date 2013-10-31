package kembe.sim;

import fj.P;
import fj.P2;
import fj.P3;
import fj.data.List;
import kembe.rand.RandomGen;

import java.util.Random;

public abstract class RandomSignalHandlerAdapter<R> extends SignalHandler{

    private final RandomGen<R> randomGen;

    protected RandomSignalHandlerAdapter(RandomGen<R> randomGen) {
        this.randomGen = randomGen;
    }


    @Override public P3<? extends SignalHandler, List<Signal>, Random> signal(Signal signal, Random random) {
        P2<R,Random> nextRandom = randomGen.next( random );
        P2<? extends SignalHandler,List<Signal>> nextState = signalRandom( signal, nextRandom._1() );
        return P.p(nextState._1(),nextState._2(),nextRandom._2());
    }

    protected abstract P2<? extends SignalHandler, List<Signal>> signalRandom(Signal signal, R randomValue);


}
