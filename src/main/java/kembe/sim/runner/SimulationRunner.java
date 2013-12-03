package kembe.sim.runner;

import fj.Effect;
import fj.F;
import fj.Show;
import fj.data.Either;
import fj.data.List;
import kembe.EventStream;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import kembe.Time;
import kembe.sim.*;
import kembe.sim.rand.Rand;
import kembe.util.Shows;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Random;


public class SimulationRunner {

    private final Instant startTime;

    private final Instant endTime;

    private final Random random;

    private final Scheduler scheduler;

    private HashMap<AgentId, SimAgent> agents;

    public SimulationRunner(Instant startTime, Instant endTime, Random random, HashMap<AgentId, SimAgent> agents, Scheduler scheduler) {
        this.random = random;
        this.agents = agents;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduler = scheduler;
    }

    public EventStream<Timed<SimEvent>> eventStream(final List<AgentInvocation> startSignals) {
        return new EventStream<Timed<SimEvent>>() {
            @Override public OpenEventStream<Timed<SimEvent>> open(final Effect<StreamEvent<Timed<SimEvent>>> effect) {

                scheduler.scheduleAt( startTime, new SchedulerTask() {
                    @Override public void run(Instant time) {
                        startSignals
                                .map( Timed.<AgentInvocation>timed( startTime ) )
                                .map( scheduleTask( effect ) )
                                .foreach( scheduler.toEffect() );
                    }
                } );


                scheduler.scheduleAt( Time.quantumIncrement( endTime ), new SchedulerTask() {
                    @Override public void run(Instant time) {
                        effect.e( StreamEvent.<Timed<SimEvent>>done() );
                    }
                } );

                final EventStream<Timed<SimEvent>> self = this;
                return new OpenEventStream<Timed<SimEvent>>() {
                    @Override public EventStream<Timed<SimEvent>> close() {
                        return self;
                    }
                };
            }
        };
    }

    private Step invokeAgent(Timed<AgentInvocation> invocation) {
        final AgentInvocation invocationData =
                invocation.value;

        final AgentId id =
                invocationData.id;

        final Instant timestamp =
                invocation.time;

        final SimAgent agent =
                agents.get( id );

        final SimAgentContext context =
                new SimAgentContext( id, timestamp );

        final Rand<Step> steps =
                agent.signal( invocationData.payload, context );

        final Step step =
                steps.next( random );

        return step;
    }

    private List<Timed<AgentInvocation>> getNextInvocations(final AgentId id, final Instant timestamp, Step step) {
        return step.action
                .either(
                        new F<SignalSchedule, List<Timed<AgentInvocation>>>() {
                            @Override public List<Timed<AgentInvocation>> f(SignalSchedule signalOccurring) {
                                return List.single(
                                        new Timed<>(
                                                signalOccurring.randomSleep.after( timestamp ).next( random ),
                                                new AgentInvocation( id, Either.<Signal, Message>left( signalOccurring.value ) ) ) );
                            }
                        }, new F<List<Message>, List<Timed<AgentInvocation>>>() {
                            @Override public List<Timed<AgentInvocation>> f(List<Message> messages) {
                                return messages.map( new F<Message, Timed<AgentInvocation>>() {
                                    @Override public Timed<AgentInvocation> f(Message message) {
                                        return new Timed<>(
                                                Time.quantumIncrement( timestamp ),
                                                new AgentInvocation( message.to, Either.<Signal, Message>right( message ) ) );
                                    }
                                } );
                            }
                        }
                );
    }

    private F<Timed<AgentInvocation>, Timed<SchedulerTask>> scheduleTask(final Effect<StreamEvent<Timed<SimEvent>>> listener) {
        return new F<Timed<AgentInvocation>, Timed<SchedulerTask>>() {
            @Override public Timed<SchedulerTask> f(Timed<AgentInvocation> agentInvocationTimed) {
                return new Timed<SchedulerTask>( agentInvocationTimed.time, new ScheduleInvocation( listener, agentInvocationTimed ) );
            }
        };
    }

    class ScheduleInvocation implements SchedulerTask {

        final Effect<StreamEvent<Timed<SimEvent>>> listener;

        final Timed<AgentInvocation> invocation;

        ScheduleInvocation(Effect<StreamEvent<Timed<SimEvent>>> listener, Timed<AgentInvocation> invocation) {
            this.listener = listener;
            this.invocation = invocation;
        }

        @Override public void run(final Instant time) {
            Step step = invokeAgent( invocation );

            step.emittedEvents.foreach( new Effect<SimEvent>() {
                @Override public void e(SimEvent simEvent) {
                    listener.e( StreamEvent.next( new Timed<>( time, simEvent ) ) );
                }
            } );

            getNextInvocations( invocation.value.id, time, step )
                    .filter( Timed.<AgentInvocation>isBeforeOrEqual( endTime ) )
                    .map( scheduleTask( listener ) )
                    .foreach( scheduler.toEffect() );
        }

        public String toString() {
            return "ScheduledInvocation of "+ invocation.value.id+" "+ Show.eitherShow( Shows.<Signal>reflectionShow(), Shows.<Message>reflectionShow() ).showS( invocation.value.payload );
        }
    }


}
