package kembe.sim.runner;

import fj.Effect;
import fj.F;
import fj.data.List;
import fj.function.Effect1;
import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.sim.*;
import kembe.sim.Signal.SignalBuilder;
import kembe.sim.SimEvent.SimEventBuilder;
import kembe.sim.rand.Rand;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Random;


public class SimulationRunner {

    private final DateTime startTime;

    private final DateTime endTime;

    private final Random random;

    private final Scheduler scheduler;

    private HashMap<AgentId, SimAgent> agents;

    public SimulationRunner(DateTime startTime, DateTime endTime, Random random, HashMap<AgentId, SimAgent> agents, Scheduler scheduler) {
        this.random = random;
        this.agents = agents;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduler = scheduler;
    }

    public EventStream<Timed<SimEvent>> eventStream(final List<Signal> startSignals) {
        return new EventStream<Timed<SimEvent>>() {
            @Override public OpenEventStream<Timed<SimEvent>> open(final EventStreamSubscriber<Timed<SimEvent>> effect) {

                scheduler.scheduleAt( startTime, time -> startSignals
                        .map( Timed.<Signal>timed( startTime ) )
                        .map( scheduleTask( effect ) )
                        .foreachDoEffect( scheduler.toEffect() ) );


                scheduler.scheduleAt( endTime.plusMillis( 1 ), time -> effect.e( StreamEvent.<Timed<SimEvent>>done() ) );

                final EventStream<Timed<SimEvent>> self = this;
                return new OpenEventStream<Timed<SimEvent>>() {
                    @Override public EventStream<Timed<SimEvent>> close() {
                        return self;
                    }
                };
            }
        };
    }

    private Step invokeAgent(final SimAgentContext context, Timed<Signal> timedSignal) {
        final Signal signal =
                timedSignal.value;

        final AgentId id =
                signal.to;

        final SimAgent agent =
                agents.get( id );

        final Rand<Step> steps =
                agent.act( signal, context );

        final Step step =
                steps.next( random );

        return step;
    }

    private List<Timed<Signal>> getNextInvocations(final SimAgentContext ctx, Step step) {
        return step.action
                .either(
                        signalOccurring -> List.single( new Timed<>(
                                signalOccurring.randomSleep.after( ctx.currentTime ).next( random ),
                                signalOccurring.value.f( ctx ) ) ),
                        messages -> messages.map( signal -> new Timed<>(
                                ctx.currentTime.plusMillis( 1 ),
                                signal.f( ctx ) ) )
                );
    }

    private F<Timed<Signal>, Timed<SchedulerTask>> scheduleTask(final EventStreamSubscriber<Timed<SimEvent>> listener) {
        return agentInvocationTimed -> new Timed<>( agentInvocationTimed.time, new ScheduleInvocation( listener, agentInvocationTimed ) );
    }

    class ScheduleInvocation implements SchedulerTask {

        final EventStreamSubscriber<Timed<SimEvent>> listener;

        final Timed<Signal> signal;

        ScheduleInvocation(EventStreamSubscriber<Timed<SimEvent>> listener, Timed<Signal> signal) {
            this.listener = listener;
            this.signal = signal;
        }

        @Override public void run(final DateTime time) {
            try {

                final SimAgentContext context =
                        new SimAgentContext( signal.value.to, signal.time );

                final Step step =
                        invokeAgent( context, signal );

                final List<Timed<Signal>> invocations =
                        getNextInvocations( context, step );

                agents.put( context.id, step.nextHandler );

                step.emittedEvents.foreachDoEffect( simEvent -> listener.e( StreamEvent.next( new Timed<>( time, simEvent.f( context ) ) ) ) );

                invocations
                        .filter( Timed.<Signal>isBeforeOrEqual( endTime.toInstant() ) )
                        .map( scheduleTask( listener ) )
                        .foreachDoEffect( scheduler.toEffect() );

            } catch (Throwable e) {
                listener.e( StreamEvent.<Timed<SimEvent>>error( new Exception( "Error during execution of simulation", e ) ) );
            }
        }

        public String toString() {
            return "ScheduledInvocation of " + signal.value.id + " " + Signal.detailShow.showS( signal.value );
        }
    }


}
