package kembe.sim.runner;

import fj.*;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import kembe.EventStream;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.Tick;
import kembe.util.Actors;
import org.joda.time.Instant;
import org.joda.time.ReadablePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InstantSimulation {
    private static final Ord<SimulatedTimeRunner> simTimeRunnerOrd = Ord.longOrd.comap( new F<SimulatedTimeRunner, Long>() {
        @Override public Long f(SimulatedTimeRunner simulatedTimeRunner) {
            return simulatedTimeRunner.time.getMillis();
        }
    } );

    private final ReadablePeriod period;

    private final Instant startTime;

    private final Instant endTime;

    private HashMap<ResourceId, HandlerAgent> handlers;

    private volatile Random random;


    public InstantSimulation(Instant startTime, Instant endTime, ReadablePeriod period, Random random, HashMap<ResourceId, HandlerAgent> agents) {
        this.random = random;
        this.handlers = agents;
        this.startTime = startTime;
        this.endTime = endTime;
        this.period = period;
    }

    private void signalTickToDrivers(final ExecutorService executor, final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {

        Instant now = startTime;
        ArrayList<Tick> ticks = new ArrayList<>();
        while (now.isBefore( endTime )) {
            Tick t = new Tick( now );
            now = now.plus( period.toPeriod().toStandardDuration() );
            ticks.add( t );
        }

        for (Tick tick : ticks) {
            actor.act( sendTick( tick ,actor,listener) );
        }
        actor.act( new SimulatedTimeRunner( now ) {
            @Override public void run() {
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

    private SimulatedTimeRunner sendSignal(final Signal signal, final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {
        return new SimulatedTimeRunner( signal.at ) {
            @Override public void run() {
                ResourceId id = signal.to;
                P2<HandlerAgent, fj.data.List<Signal>> result = handlers.get( id ).signal( signal, random );
                handlers.put( id, result._1() );
                scheduleSignals( result._2(), actor, listener );
                listener.e( StreamEvent.next( signal ) );
            }
        };
    }

    private SimulatedTimeRunner sendTick(final Tick tick,final Actor<SimulatedTimeRunner> actor, final Effect<StreamEvent<Signal>> listener) {
        return new SimulatedTimeRunner( tick.tickTime ) {
            @Override public void run() {
                for (HandlerAgent agent : handlers.values()) {
                    P2<HandlerAgent, fj.data.List<Signal>> result = agent.tick( tick, random );
                    handlers.put( result._1().id, result._1() );
                    scheduleSignals( result._2(),actor,listener );
                }
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

                signalTickToDrivers( service, actor, effect );
                try {
                    service.awaitTermination( 5, TimeUnit.MINUTES );
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


        public final Instant time;

        SimulatedTimeRunner(Instant time) {
            this.time = time;
        }

    }
}
