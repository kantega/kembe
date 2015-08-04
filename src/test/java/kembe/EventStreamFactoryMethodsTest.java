package kembe;

import fj.F;
import fj.P;
import fj.Show;
import fj.data.Either;
import fj.data.Stream;
import fj.function.Integers;
import kembe.util.Split;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EventStreamFactoryMethodsTest {

    @Test
    public void fromStream() throws InterruptedException {
        int from = 0;
        int to = 3;
        final CountDownLatch nextLatch = new CountDownLatch( to );
        final CountDownLatch doneLatch = new CountDownLatch( 1 );
        EventStream
                .fromStream( Stream.range( from, to ) )
                .open( handler( Integer.class, nextLatch, doneLatch ) );

        Asserts.awaitAssert( 1, TimeUnit.SECONDS, nextLatch, doneLatch );
    }

    @Test
    public void mappedFromStream() throws InterruptedException {
        int from = 0;
        int to = 3;
        final CountDownLatch nextLatch = new CountDownLatch( to );
        final CountDownLatch doneLatch = new CountDownLatch( 1 );
        EventStream
                .fromStream( Stream.range( from, to ) )
                .map( Show.intShow.showS_() )
                .tap(EventStreamSubscriber.subscriber().onNext( System.out::println ))
                .open( handler( String.class, nextLatch, doneLatch ) );


        Asserts.awaitAssert( 1, TimeUnit.SECONDS, nextLatch, doneLatch );
    }

    @Test
    public void mappedFilteredFromStream() throws InterruptedException {
        int from = 0;
        int to = 3;
        final CountDownLatch nextLatch = new CountDownLatch( to / 2 );
        final CountDownLatch doneLatch = new CountDownLatch( 1 );
        EventStream
                .fromStream( Stream.range( from, to ) )
                .filter( Integers.even )
                .map( Show.intShow.showS_() )

                .open( handler( String.class, nextLatch, doneLatch ) );

        Asserts.awaitAssert( 1, TimeUnit.SECONDS, nextLatch, doneLatch );
    }

    @Test
    public void joinedFromStream() throws InterruptedException {
        int from = 0;
        int to = 3;
        final CountDownLatch nextLatch = new CountDownLatch( to * 2 );
        final CountDownLatch doneLatch = new CountDownLatch( 1 );
        EventStream<Integer> one =
                EventStream
                        .fromStream( Stream.range( from, to ) );

        EventStream<Integer> two =
                EventStream
                        .fromStream( Stream.range( from, to ) );

        EventStream.merge( one, two )
                .open( handler( Integer.class, nextLatch, doneLatch ) );


        Asserts.awaitAssert( 1, TimeUnit.SECONDS, nextLatch, doneLatch );
    }

    @Test
    public void splitFromStream() throws InterruptedException {
        int from = 0;
        int to = 3;
        final CountDownLatch nextLatch1 = new CountDownLatch( to / 2 );
        final CountDownLatch doneLatch1 = new CountDownLatch( 1 );
        final CountDownLatch nextLatch2 = new CountDownLatch( to / 2 );
        final CountDownLatch doneLatch2 = new CountDownLatch( 1 );
        Split<Integer, Integer> streams =
                EventStream.split( EventStream
                        .fromStream( Stream.range( from, to ) )
                        .map( integer -> Either.iif( Integers.even.f( integer ), P.p( integer ), P.p( integer ) ) ) );

        streams._1().open( handler( Integer.class, nextLatch1, doneLatch1 ) );
        streams._2().open( handler( Integer.class, nextLatch2, doneLatch2 ) );


        Asserts.awaitAssert( 1, TimeUnit.SECONDS, nextLatch1, doneLatch1, nextLatch2, doneLatch2 );
    }

    private <A> EventStreamSubscriber<A> handler(Class<A> type, final CountDownLatch nextCounter, final CountDownLatch doneCounter) {
        return EventStreamSubscriber.create( new EventStreamHandler<A>() {
            @Override
            public void next(A evt) {
                nextCounter.countDown();
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException( "Should not happen" );
            }

            @Override
            public void done() {
                doneCounter.countDown();
            }
        } );
    }
}
