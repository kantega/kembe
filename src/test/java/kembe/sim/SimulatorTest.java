package kembe.sim;

import fj.Show;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.sim.rand.Rand;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Seconds;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static kembe.Time.now;
import static kembe.sim.AgentId.idFromString;
import static kembe.sim.RandWait.waitAtMost;
import static kembe.sim.RandWait.waitFor;
import static kembe.sim.Signal.signal;

public class SimulatorTest {

    private SimAgent testDriver =
            new SimAgent() {
                @Override public Rand<Step> act(Signal s, SimAgentContext ctx) {
                    if (s.msg.equals( "send" ))
                        return just( send(
                                signal( AgentId.idFromString( "testHandler" ), "GET to the chopper" ),
                                event( "Request sent" ) ) );

                    else if (s.msg.startsWith( "OK" ))
                        return just( sleep(
                                waitAtMost( Seconds.ONE ), Signal.newToSelf( "retry" ),
                                event( "Reply received" ) ) );

                    else
                        return alt( 5, sleep( waitFor( Seconds.ONE ), Signal.newToSelf( "send" ) ) )
                                .or( 5, sleep( waitFor( Seconds.ONE ), Signal.newToSelf( "retry" ) ) );
                }
            };

    private SimAgent testHandler =
            new SimAgent() {
                @Override public Rand<Step> act(Signal signal, SimAgentContext ctx) {
                    if (signal.msg.startsWith( "GET" ))
                        return just( sleep( waitAtMost( Duration.millis( 5 ) ), Signal.reply( signal, "doReply" ) ) );

                    if (signal.msg.equals( "doReply" ))
                        return just( send( signal.reply( "OK" ) ) );

                    return just( sleep( waitFor( Seconds.ONE ), Signal.newToSelf( "noop") ) );
                }
            };

    @Test
    public void instant() throws InterruptedException {

        Random r = new Random( 2 );
        DateTime now = now().toDateTime();
        final CountDownLatch l = new CountDownLatch( 1 );
        final Show<Timed<SimEvent>> show = Timed.elapsedShow( now.toInstant(), SimEvent.plainShow );//Timed.show( SimEvent.plainShow );//
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
        l.await( 5, TimeUnit.SECONDS );
    }

    //@Test
    public void realtime() throws InterruptedException {

        Random r = new Random( 2 );
        DateTime now = now().toDateTime();
        final Show<Timed<SimEvent>> show = Timed.elapsedShow( now.toInstant(), SimEvent.plainShow );
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
