package kembe.sim.agents.service;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.sim.rand.RandomGen;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import kembe.sim.agents.SignalHandlerContext;
import org.joda.time.Duration;

import java.util.Random;

public class WorkingIndependentService extends SignalHandler {

    private final List<Signal> queue;

    private final RandomGen<Duration> randomDuration;

    public WorkingIndependentService(List<Signal> queue, RandomGen<Duration> randomDuration) {
        this.queue = queue;
        this.randomDuration = randomDuration;
    }

    @Override public P2<SignalHandler, List<Signal>> signal(Signal signal, Random random, SignalHandlerContext context) {
        if (signal.msg.equals( "done" )) {


            if (queue.isEmpty())
                return P.p( idle(), List.list( signal.followImmediately( signal.prev.some().from, "ok" ) ) );
            else {
                Duration duration = randomDuration.next( random );
                return P.p(
                        dequeued(),
                        List.list(
                                signal.followImmediately( signal.prev.some().from, "ok" ),
                                Signal.newSignalFromPrevious( queue.head(), context.id, context.currentTime.plus( duration ), "done" )
                        ));
            }
        }
        else {
            return nextState( enqueued( signal ) );
        }
    }

    private SignalHandler enqueued(Signal signal) {
        return new WorkingIndependentService( queue.snoc( signal ), randomDuration );
    }

    private SignalHandler dequeued() {
        return new WorkingIndependentService( queue.tail(), randomDuration );
    }

    private SignalHandler idle() {
        return new IdleIndependentService( randomDuration );
    }
}
