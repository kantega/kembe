package kembe.sim.runner;

import fj.P;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import kembe.Time;
import kembe.sim.*;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.Random;

public class HandlerAgent {
    public final ResourceId id;

    public final Instant lastexecution;

    public final SignalHandler handler;

    public HandlerAgent(ResourceId id, Instant lastexecution, SignalHandler handler) {
        this.id = id;
        this.lastexecution = lastexecution;
        this.handler = handler;
    }

    public P2<HandlerAgent, List<Signal>> signal(Signal signal, Random random) {
        P2<SignalHandler, List<Signal>> nextState =
                handler.signal( Either.<Tick, Signal>right( signal ), random, new SignalHandlerContext( id, lastexecution, Time.quantumIncrement( signal.at ), new Interval( lastexecution, signal.at ) ) );
        return P.p( new HandlerAgent( id, signal.at, nextState._1() ), nextState._2() );
    }

    public P2<HandlerAgent, List<Signal>> tick(Tick tick, Random random) {
        P2<SignalHandler, List<Signal>> nextState =
                handler.signal( Either.<Tick, Signal>left( tick ), random, new SignalHandlerContext( id, lastexecution, Time.quantumIncrement( tick.tickTime ), new Interval( lastexecution, tick.tickTime ) ) );
        return P.p( new HandlerAgent( id, tick.tickTime, nextState._1() ), nextState._2() );
    }
}
