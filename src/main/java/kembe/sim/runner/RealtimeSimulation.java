package kembe.sim.runner;

import fj.Effect;
import fj.P3;
import fj.data.List;
import org.joda.time.Instant;
import kembe.*;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;

import java.util.*;

public class RealtimeSimulation {

    private HashMap<ResourceId, SignalHandler> drivers;
    private HashMap<ResourceId, SignalHandler> handlers;
    private EventStream<Instant> ticks;
    private volatile Random random;


    public RealtimeSimulation(Random random, HashMap<ResourceId, SignalHandler> drivers, HashMap<ResourceId, SignalHandler> handlers, EventStream<Instant> ticks) {
        this.random = random;
        this.drivers = drivers;
        this.handlers = handlers;
        this.ticks = ticks;
    }

    private TimerTask signalTickToDrivers(final Instant instant, final Timer timer,final Effect<StreamEvent<Signal>> listener) {
        return new TimerTask() {
            @Override public void run() {

                HashMap<ResourceId, SignalHandler> newDrivers = new HashMap<>();

                ArrayList<Signal> newSignals = new ArrayList<>();

                for (Map.Entry<ResourceId, SignalHandler> entry : drivers.entrySet()) {
                    P3<? extends SignalHandler, List<Signal>, Random> result = entry.getValue().signal( Signal.newSignal(  entry.getKey() , ResourceId.fromString( "Runner" ), instant, "tick" ), random );
                    newDrivers.put( entry.getKey(), result._1() );
                    random = result._3();
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
                P3<? extends SignalHandler, List<Signal>, Random> result = handlers.get( id ).signal( signal, random );
                random = result._3();
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
