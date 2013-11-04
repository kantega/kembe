package kembe.sim;

import fj.data.List;
import kembe.EventStream;
import kembe.sim.agents.Agent;
import kembe.sim.runner.InstantSimulation;
import kembe.sim.runner.RealtimeSimulation;
import org.joda.time.Instant;
import org.joda.time.ReadablePeriod;

import java.util.HashMap;
import java.util.Random;

public class SimulationBuilder {

    private HashMap<ResourceId, Agent> drivers = new HashMap<>();

    private HashMap<ResourceId, Agent> handlers = new HashMap<>();

    private SimulationBuilder() {
    }

    public static SimulationBuilder build(){
        return new SimulationBuilder();
    }

    public SimulationBuilder addDriver(Agent driver) {
        drivers.put( driver.id, driver );
        return addHandler( driver );
    }

    public SimulationBuilder addHandler(Agent agent) {
        handlers.put( agent.id, agent );
        return this;
    }

    public SimulationBuilder addDrivers(List<Agent> drivers) {
        for (Agent a : drivers)
            addDriver( a );
        return this;
    }

    public SimulationBuilder addHandlers(List<Agent> agents) {
        for (Agent a : agents)
            addHandler( a );
        return this;
    }

    public EventStream<Signal> realtime(EventStream<Instant> ticks,Random random) {
        return new RealtimeSimulation( random, drivers, handlers, ticks ).signals();
    }

    public EventStream<Signal> instant(Instant start, Instant stop, ReadablePeriod period, Random random) {
        return new InstantSimulation( start, stop, period, random, drivers, handlers ).signals();
    }
}
