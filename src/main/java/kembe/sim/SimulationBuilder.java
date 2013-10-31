package kembe.sim;

import org.joda.time.Instant;
import org.joda.time.ReadablePeriod;
import kembe.EventStream;
import kembe.sim.runner.InstantSimulation;
import kembe.sim.runner.RealtimeSimulation;

import java.util.HashMap;
import java.util.Random;

public class SimulationBuilder {

    private HashMap<ResourceId, SignalHandler> drivers = new HashMap<>(  );

    private HashMap<ResourceId,SignalHandler> handlers = new HashMap<>(  );

    private final EventStream<Instant> ticks;

    public SimulationBuilder(EventStream<Instant> ticks){
        this.ticks = ticks;
    }

    public SimulationBuilder addDriver(ResourceId id, SignalHandler driver){
        drivers.put( id, driver );
        return addHandler( id,driver );
    }

    public SimulationBuilder addHandler(ResourceId id, SignalHandler handler){
        handlers.put(id,handler);
        return this;
    }


    public EventStream<Signal> realtime(Random random){
        return new RealtimeSimulation( random,drivers,handlers,ticks ).signals();
    }

    public EventStream<Signal> instant( Instant start, Instant stop, ReadablePeriod period,Random random){
        return new InstantSimulation( start,stop,period,random,drivers,handlers ).signals();
    }
}
