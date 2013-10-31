package no.kantega.kembe.sim;

import fj.Equal;
import fj.F;
import fj.P;
import fj.P2;
import fj.data.List;
import kembe.rand.RandomGen;
import org.joda.time.Instant;
import org.joda.time.Seconds;
import org.junit.Test;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.Time;
import kembe.scheduler.Clock;
import kembe.sim.*;

import java.util.Random;

import static fj.P.p;
import static fj.data.List.list;

public class InstantSimulatorTest {
    private RandomGen<Boolean> oneOfTen =
            RandomGen.randomInt( 0, 9 ).map( new F<Integer, Boolean>() {
                @Override public Boolean f(Integer integer) {
                    if (integer == 0)
                        return true;
                    else
                        return false;
                }
            } );
    private SignalHandler testDriver =
            new RandomSignalHandlerAdapter<Boolean>( oneOfTen ) {
                @Override protected P2<? extends SignalHandler, List<Signal>> signalRandom(Signal signal, Boolean randomValue) {
                    if (signal.msg.startsWith( "OK" ))
                        return p( this, List.<Signal>nil() );
                    else if (randomValue)
                        return p( this, list( signal.follow( ResourceId.fromString( "testHandler" ), Time.quantumIncrement( signal.at ), "GET to the chopper" ) ) );
                    else
                        return p( this, List.<Signal>nil() );
                }
            };
    private SignalHandler testHandler =
            new NonrandomSignalHandler() {
                @Override protected P2<? extends SignalHandler, List<Signal>> signal(Signal signal) {

                    final SignalHandler self = this;

                    if (signal.msg.startsWith( "GET" ))
                        return P.p( new NonrandomSignalHandler() {

                            @Override protected P2<? extends SignalHandler, List<Signal>> signal(Signal signal) {
                                if (Equal.stringEqual.eq( signal.msg, "self" ))
                                    return P.p( self, list( signal.follow( ResourceId.fromString( "testDriver" ), Time.quantumIncrement( signal.at ), "OK" ) ) );
                                else
                                    return P.p( this, List.<Signal>nil() );
                            }
                        }, list( signal.follow( ResourceId.fromString( "testHandler" ), signal.at.plus( Seconds.ONE.toStandardDuration() ), "self" ) ) );

                    return P.p( this, List.<Signal>nil() );
                }
            };

    @Test
    public void simple() throws InterruptedException {


        Random r = new Random( 2 );

        SimulationBuilder builder =
                new SimulationBuilder( Clock.seconds() )
                        .addDriver( ResourceId.fromString( "testDriver" ), testDriver )
                        .addHandler( ResourceId.fromString( "testHandler" ), testHandler );
        Instant now = Time.now();

        final OpenEventStream<Signal> openStream =
                builder
                        .instant( now, now.plus( Seconds.seconds( 60 ).toStandardDuration() ), Seconds.ONE, r )
                        .filter( new F<Signal, Boolean>() {
                            @Override public Boolean f(Signal signal) {
                                return !signal.msg.startsWith( "tick" );
                            }
                        } )
                        .open( EventStreamSubscriber.create( new EventStreamHandler<Signal>() {
                            @Override public void next(Signal signal) {
                                System.out.println( signal.at + ": " + Signal.chainShow.showS( signal ) + " ( " + Time.now().toString() + " )" );

                            }

                            @Override public void error(Exception e) {

                            }

                            @Override public void done() {

                            }
                        } ) );
    }

}
