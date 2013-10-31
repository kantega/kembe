package kembe.sim.runner;

import fj.*;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import fj.data.List;
import org.joda.time.Instant;
import org.joda.time.ReadablePeriod;
import kembe.EventStream;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import kembe.util.Actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InstantSimulation {
    private static final Ord<SimulatedTimeRunner> simTimeRunnerOrd = Signal.signalOrd.comap( new F<SimulatedTimeRunner, Signal>() {
        @Override public Signal f(SimulatedTimeRunner simulatedTimeRunner) {
            return simulatedTimeRunner.signal;
        }
    } );
    private final ReadablePeriod period;
    private final Instant startTime;
    private final Instant endTime;
    private HashMap<ResourceId, SignalHandler> drivers;
    private HashMap<ResourceId, SignalHandler> handlers;
    private volatile Random random;


    public InstantSimulation(Instant startTime, Instant endTime, ReadablePeriod period, Random random, HashMap<ResourceId, SignalHandler> drivers, HashMap<ResourceId, SignalHandler> handlers) {
        this.random = random;
        this.drivers = drivers;
        this.handlers = handlers;
        this.startTime = startTime;
        this.endTime = endTime;
        this.period = period;
    }

    private void signalTickToDrivers(final ExecutorService executor, final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {

        Instant now = startTime;
        ArrayList<List<Signal>> ticks = new ArrayList<>();
        while (now.isBefore( endTime )) {
            final Instant simulatedTime = now;
            List<Signal> signals = List.iterableList( new ArrayList<>( drivers.keySet() ) ).map( new F<ResourceId, Signal>() {
                @Override public Signal f(ResourceId resourceId) {
                    return Signal.newSignal( resourceId, ResourceId.fromString( "Runner" ), simulatedTime, "tick" );
                }
            } );
            now = now.plus( period.toPeriod().toStandardDuration() );
            ticks.add( signals );
        }

        for (List<Signal> signals : ticks) {
            scheduleSignals( signals, actor, listener );
        }
        actor.act( new SimulatedTimeRunner( Signal.newSignal( ResourceId.fromString( "*" ), ResourceId.fromString( "Runner" ), now, "Simulation terminated" ) ) {
            @Override protected void run(Signal signal) {
                executor.shutdownNow();
            }
        } );


    }

    private void scheduleSignals(final fj.data.List<Signal> signals, final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {
        for (Signal signal : signals) {
            if (signal.at.isBefore( endTime ))
                actor.act( sendSignal( signal, actor, listener ) );
        }
    }

    private SimulatedTimeRunner sendSignal(final Signal s, final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {
        return new SimulatedTimeRunner( s ) {
            @Override public void run(Signal signal) {
                ResourceId id = signal.to;
                P3<? extends SignalHandler, fj.data.List<Signal>, Random> result = handlers.get( id ).signal( signal, random );
                random = result._3();
                handlers.put( id, result._1() );
                scheduleSignals( result._2(), actor, listener );
                listener.e( StreamEvent.next( signal ) );
            }
        };
    }

    public EventStream<Signal> signals() {
        return new EventStream<Signal>() {
            @Override public OpenEventStream<Signal> open(final Effect<StreamEvent<Signal>> effect) {

                final EventStream<Signal> self = this;
                final ExecutorService service = Executors.newSingleThreadExecutor();

                final Actor<SimulatedTimeRunner> actor = Actors.orderedActor( Strategy.<Unit>executorStrategy( service ), simTimeRunnerOrd, new Effect<SimulatedTimeRunner>() {
                    @Override public void e(SimulatedTimeRunner runnable) {
                        runnable.run();
                    }
                } );

                signalTickToDrivers(service, actor, effect );
                try {
                    service.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    effect.e( StreamEvent.<Signal>error( e ) );
                }
                return new OpenEventStream<Signal>() {
                    @Override public EventStream<Signal> close() {
                        service.shutdownNow();
                        return self;
                    }
                };
            }
        };
    }

    static abstract class SimulatedTimeRunner implements Runnable {


        public final Signal signal;

        SimulatedTimeRunner(Signal signal) {
            this.signal = signal;
        }

        @Override public void run() {
            run( signal );
        }

        protected abstract void run(Signal signal);
    }
}
