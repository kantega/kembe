package kembe.sim.agents.service;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.rand.RandomGen;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import kembe.sim.agents.SignalHandlerContext;
import org.joda.time.Duration;

import java.util.Random;

public class IdleIndependentService extends SignalHandler {

    private final RandomGen<Duration> randomDuration;

    public IdleIndependentService(RandomGen<Duration> randomDuration) {
        this.randomDuration = randomDuration;
    }

    @Override public P2<SignalHandler, List<Signal>> signal(Signal signal, Random random, SignalHandlerContext context) {
        Duration duration = randomDuration.next( random );
        return P.p( startWork(), List.list( Signal.newSignalFromPrevious( signal, context.id, context.currentTime.plus( duration ), "done" ) )  );
    }

    private SignalHandler startWork(){
        return new WorkingIndependentService( List.<Signal>nil(),randomDuration );
    }
}
