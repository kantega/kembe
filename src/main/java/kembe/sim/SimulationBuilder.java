package kembe.sim;

import fj.F;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import kembe.EventStream;
import kembe.sim.runner.AgentInvocation;
import kembe.sim.runner.Scheduler;
import kembe.sim.runner.SimulationRunner;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Random;

public class SimulationBuilder {


    private HashMap<AgentId, SimAgent> agents = new HashMap<>();

    private SimulationBuilder() {
    }

    public static SimulationBuilder build(){
        return new SimulationBuilder();
    }


    public SimulationBuilder addHandler(AgentId id, SimAgent handlerAgent) {
        agents.put( id, handlerAgent );
        return this;
    }

    public SimulationBuilder addHandlers(List<P2<AgentId,SimAgent>> agents) {
        for (P2<AgentId,SimAgent> a : agents)
            addHandler( a._1(),a._2() );
        return this;
    }


    public EventStream<Timed<SimEvent>> instant(Instant start, Instant stop, Random random,final String startSignal) {

       List<AgentInvocation> startSignals = List.iterableList( agents.keySet()).map(new F<AgentId, AgentInvocation>() {
           @Override public AgentInvocation f(AgentId agentId) {
               return new AgentInvocation( agentId, Either.<Signal,Message>left(Signal.newSignal( startSignal )) );
           }
       });

        return new SimulationRunner( start, stop, random, agents, Scheduler.instantScheduler() ).eventStream( startSignals );
    }

    public EventStream<Timed<SimEvent>> realtime(Instant start, Instant stop, Random random,final String startSignal    ) {

        List<AgentInvocation> startSignals = List.iterableList( agents.keySet()).map(new F<AgentId, AgentInvocation>() {
            @Override public AgentInvocation f(AgentId agentId) {
                return new AgentInvocation( agentId, Either.<Signal,Message>left(Signal.newSignal( startSignal )) );
            }
        });

        return new SimulationRunner( start, stop, random, agents, Scheduler.realtimeScheduler() ).eventStream( startSignals );
    }
}
