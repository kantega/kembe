package kembe.sim;

import fj.data.List;
import kembe.EventStream;
import kembe.sim.runner.HandlerAgent;
import kembe.sim.runner.InstantSimulation;
import kembe.sim.runner.RealtimeSimulation;
import org.joda.time.Instant;
import org.joda.time.ReadablePeriod;

import java.util.HashMap;
import java.util.Random;

public class SimulationBuilder {


    private HashMap<ResourceId, HandlerAgent> handlers = new HashMap<>();

    private SimulationBuilder() {
    }

    public static SimulationBuilder build(){
        return new SimulationBuilder();
    }


    public SimulationBuilder addHandler(HandlerAgent handlerAgent) {
        handlers.put( handlerAgent.id, handlerAgent );
        return this;
    }

    public SimulationBuilder addHandlers(List<HandlerAgent> agents) {
        for (HandlerAgent a : agents)
            addHandler( a );
        return this;
    }

    public EventStream<Signal> realtime(EventStream<Instant> ticks,Random random) {
        return new RealtimeSimulation( random, handlers, ticks ).signals();
    }

    public EventStream<Signal> instant(Instant start, Instant stop, ReadablePeriod period, Random random) {
        return new InstantSimulation( start, stop, period, random, handlers ).signals();
    }
}
