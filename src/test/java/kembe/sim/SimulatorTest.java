package kembe.sim;

import fj.Show;
import fj.data.Either;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.sim.rand.Rand;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Seconds;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static kembe.Time.now;
import static kembe.sim.AgentId.idFromString;
import static kembe.sim.Message.message;
import static kembe.sim.RandWait.waitFor;
import static kembe.sim.RandWait.waitForAtLeast;

public class SimulatorTest {

    private SimAgent testDriver =
            new SimAgent() {
                @Override public Rand<Step> signal(Either<Signal, Message> e, SimAgentContext ctx) {
                    if (Signal.is( "send", e ).isSome())
                        return just( send(
                                message( AgentId.idFromString( "testHandler" ), "GET to the chopper", ctx ),
                                event( "Request sent", ctx ) ) );

                    else if (Message.contains( "OK", e ).isSome())
                        return just( sleep( waitForAtLeast( Seconds.ONE ), "retry", event( "Reply received", ctx ) ) );

                    else
                        return alt( 5, sleep( waitFor( Seconds.ONE ), "send" ) )
                                .or( 5, sleep( waitFor( Seconds.ONE ), "retry" ) );
                }
            };

    private SimAgent testHandler =
            new SimAgent() {
                @Override public Rand<Step> signal(Either<Signal, Message> message, SimAgentContext ctx) {
                    for (Message m : Message.contains( "GET", message ))
                        return just( sleep( waitForAtLeast( Duration.millis( 5 ) ), "doReply", m ) );

                    for (Signal s : Signal.is( "doReply", message ))
                        return just( send( s.msg.some().reply( "OK" ) ) );

                    return just( sleep( waitFor( Seconds.ONE ), "noop" ) );
                }
            };

    @Test
    public void instant() throws InterruptedException {


        Random r = new Random( 2 );
        Instant now = now();
        final CountDownLatch l = new CountDownLatch( 1 );
        final Show<Timed<SimEvent>> show = Timed.elapsedShow( now, SimEvent.plainShow );//Timed.show( SimEvent.plainShow );//
        SimulationBuilder.build()
                .addHandler( idFromString( "testDriver" ), testDriver )
                .addHandler( idFromString( "testHandler" ), testHandler )
                .instant( now, now.plus( Seconds.seconds( 30 ).toStandardDuration() ), r, "start" )
                .open( EventStreamSubscriber.create( new EventStreamHandler<Timed<SimEvent>>() {
                    @Override public void next(Timed<SimEvent> message) {
                        show.printlnE( message );
                    }

                    @Override public void error(Exception e) {
                        e.printStackTrace();
                    }

                    @Override public void done() {
                        System.out.println( "Done" );
                        l.countDown();
                    }
                } ) );
        l.await( 5, TimeUnit.HOURS );
    }

    //@Test
    public void realtime() throws InterruptedException {


        Random r = new Random( 2 );
        Instant now = now();
        final Show<Timed<SimEvent>> show = Timed.elapsedShow( now, SimEvent.plainShow );
        final CountDownLatch l = new CountDownLatch( 1 );
        SimulationBuilder.build()
                .addHandler( idFromString( "testDriver" ), testDriver )
                .addHandler( idFromString( "testHandler" ), testHandler )
                .realtime( now, now.plus( Seconds.seconds( 30 ).toStandardDuration() ), r, "start" )
                .open( EventStreamSubscriber.create( new EventStreamHandler<Timed<SimEvent>>() {
                    @Override public void next(Timed<SimEvent> message) {
                        show.println( message );
                    }

                    @Override public void error(Exception e) {
                        e.printStackTrace();
                    }

                    @Override public void done() {
                        System.out.println( "Done" );
                        l.countDown();
                    }
                } ) );
        l.await( 30, TimeUnit.SECONDS );
    }

}
