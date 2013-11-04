package kembe.sim.runner;

import fj.Effect;
import fj.P2;
import fj.data.List;
import kembe.*;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.agents.Agent;
import org.joda.time.Instant;

import java.util.*;

public class RealtimeSimulation {

    private volatile HashMap<ResourceId, Agent> drivers;
    private final HashMap<ResourceId, Agent> handlers;
    private final EventStream<Instant> ticks;
    private final Random random;


    public RealtimeSimulation(Random random, HashMap<ResourceId, Agent> drivers, HashMap<ResourceId, Agent> handlers, EventStream<Instant> ticks) {
        this.random = random;
        this.drivers = drivers;
        this.handlers = handlers;
        this.ticks = ticks;
    }

    private TimerTask signalTickToDrivers(final Instant instant, final Timer timer,final Effect<StreamEvent<Signal>> listener) {
        return new TimerTask() {
            @Override public void run() {

                HashMap<ResourceId, Agent> newDrivers = new HashMap<>();

                ArrayList<Signal> newSignals = new ArrayList<>();

                for (Map.Entry<ResourceId, Agent> entry : drivers.entrySet()) {
                    P2<Agent, List<Signal>> result = entry.getValue().signal( Signal.newSignal(  entry.getKey() , ResourceId.fromString( "Runner" ), instant, "tick" ), random );
                    newDrivers.put( entry.getKey(), result._1() );
                    newSignals.addAll( result._2().toCollection() );
                }

                drivers = newDrivers;
                scheduleSignals( List.iterableList( newSignals ), timer,listener );
            }
        };
    }

    private void scheduleSignals(final List<Signal> signals, Timer timer,final Effect<StreamEvent<Signal>> listener) {
        for (Signal signal : signals) {
            timer.schedule( sendSignal( signal, timer ,listener), signal.at.toDate() );
        }
    }

    private TimerTask sendSignal(final Signal signal, final Timer timer,final Effect<StreamEvent<Signal>> listener) {
        return new TimerTask() {
            @Override public void run() {
                ResourceId id = signal.to;
                P2<Agent, List<Signal>> result = handlers.get( id ).signal( signal, random );
                handlers.put( id, result._1() );
                scheduleSignals( result._2(), timer,listener );
                listener.e( StreamEvent.next( signal ) );
            }
        };
    }

    public EventStream<Signal> signals() {
        return new EventStream<Signal>() {
            @Override public OpenEventStream<Signal> open(final Effect<StreamEvent<Signal>> effect) {

                final EventStream<Signal> self = this;
                final Timer timer = new Timer( "Realtime sumulation thread" );
                final OpenEventStream<Instant> tickStream = ticks.open( EventStreamSubscriber.create( new EventStreamHandler<Instant>() {
                    @Override public void next(Instant instant) {
                        timer.schedule( signalTickToDrivers( instant, timer,effect ), 0 );
                    }

                    @Override public void error(Exception e) {
                        effect.e( StreamEvent.<Signal>error( e ) );
                    }

                    @Override public void done() {

                    }
                } ) );

                return new OpenEventStream<Signal>() {
                    @Override public EventStream<Signal> close() {
                        tickStream.close();
                        timer.cancel();
                        return self;
                    }
                };
            }
        };
    }


}
