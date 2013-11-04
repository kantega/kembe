package kembe.sim.agents;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.Time;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.Random;

public class Agent {
    public final ResourceId id;

    public final Instant lastexecution;

    public final SignalHandler handler;

    public Agent(ResourceId id, Instant lastexecution, SignalHandler handler) {
        this.id = id;
        this.lastexecution = lastexecution;
        this.handler = handler;
    }

    public P2<Agent, List<Signal>> signal(Signal signal, Random random) {
        P2<SignalHandler, List<Signal>> nextState = handler.signal( signal, random, new SignalHandlerContext( id, lastexecution, Time.quantumIncrement( signal.at ), new Interval( lastexecution, signal.at ) ) );
        return P.p( new Agent( id, signal.at, nextState._1() ), nextState._2() );
    }
}
