package kembe.sim;

import fj.Equal;
import fj.F;
import fj.P;
import fj.P2;
import fj.data.List;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.Time;
import kembe.scheduler.Clock;
import kembe.sim.rand.RandomGen;
import kembe.sim.runner.HandlerAgent;
import org.joda.time.Instant;
import org.joda.time.Seconds;
import org.junit.Test;

import java.util.Random;

import static fj.P.p;
import static fj.data.List.list;

public class RealtimeSimulatorTest {
    private RandomGen<Boolean> oneOfTen =
            RandomGen.randomInt( 0, 9 ).map( new F<Integer, Boolean>() {
                @Override public Boolean f(Integer integer) {
                    return (integer == 0);
                }
            } );

    private SignalHandler testDriver =
            new RandomSignalHandlerAdapter<Boolean>( oneOfTen ) {
                @Override protected P2<SignalHandler, List<Signal>> signalRandom(Signal signal, Boolean randomValue, SignalHandlerContext context) {
                    if (signal.msg.startsWith( "OK" ))
                        return p( self(), List.<Signal>nil() );
                    else
                        return p( self(), List.<Signal>nil() );
                }

                @Override protected P2<SignalHandler, List<Signal>> tick(Tick tick, Boolean randomValue, SignalHandlerContext context) {
                    if (randomValue)
                        return p( self(), list( Signal.newSignal( ResourceId.fromString( "testHandler" ), context.id, Time.quantumIncrement( tick.tickTime ), "GET to the chopper" ) ) );
                    else
                        return p( self(), List.<Signal>nil() );
                }
            };

    private SignalHandler testHandler =
            new NonrandomSignalHandler() {
                @Override protected P2<SignalHandler, List<Signal>> signal(Signal signal, SignalHandlerContext context) {

                    final SignalHandler self = this;

                    if (signal.msg.startsWith( "GET" ))
                        return P.p( new NonrandomSignalHandler() {

                            @Override protected P2<SignalHandler, List<Signal>> signal(Signal signal, SignalHandlerContext context) {
                                if (Equal.stringEqual.eq( signal.msg, "self" ))
                                    return P.p( self, list( signal.follow( ResourceId.fromString( "testDriver" ), Time.quantumIncrement( signal.at ), "OK" ) ) );
                                else
                                    return P.p( self(), List.<Signal>nil() );
                            }

                            @Override protected P2<SignalHandler, List<Signal>> tick(Tick tick, SignalHandlerContext context) {
                                return P.p( self(), List.<Signal>nil() );
                            }
                        }.self(), list( signal.follow( ResourceId.fromString( "testHandler" ), signal.at.plus( Seconds.ONE.toStandardDuration() ), "self" ) ) );

                    return P.p( self(), List.<Signal>nil() );
                }

                @Override protected P2<SignalHandler, List<Signal>> tick(Tick tick, SignalHandlerContext context) {
                    return P.p( self(), List.<Signal>nil() );
                }
            };


    @Test
    public void simple() throws InterruptedException {


        Random r = new Random( 2 );
        Instant start = Time.now();
        SimulationBuilder.build()
                .addHandler( new HandlerAgent( ResourceId.fromString( "testDriver" ), start, testDriver ) )
                .addHandler( new HandlerAgent( ResourceId.fromString( "testHandler" ), start, testHandler ) )
                .realtime( Clock.seconds(), r )
                .open( EventStreamSubscriber.create( new EventStreamHandler<Signal>() {
                    @Override public void next(Signal signal) {
                        System.out.println( signal.at + ": " + Signal.chainShow.showS( signal ) + " ( " + Time.now().toString() + " )" );
                    }

                    @Override public void error(Exception e) {

                    }

                    @Override public void done() {

                    }
                } ) );

        Thread.sleep( 60000 );

    }


}
