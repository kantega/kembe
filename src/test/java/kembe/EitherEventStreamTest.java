package kembe;

import fj.data.Either;
import fj.data.Stream;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EitherEventStreamTest {

    @Test
    public void testSerializationOfEvents() throws InterruptedException {


        AtomicInteger counter = new AtomicInteger(  );

        EventBus<String> one = new EventBus<>();
        EventBus<String> other = new EventBus<>();

        EventStream<Either<String,String>> both = one.stream().or( other.stream() );


        both.open( EventStreamSubscriber.<Either<String, String>>subscriber().onNext( either -> {
            System.out.println("ENTER "+Thread.currentThread().getName()+ " - " +either.either( String::toString,String::toString ));
            if (counter.getAndIncrement() > 0) {
                System.out.println("NOO");
                throw new ConcurrentModificationException( "Two threads at same time" );

            }
            try {
                Thread.sleep( 100 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter.decrementAndGet();
            System.out.println( "EXIT " + Thread.currentThread().getName() + " - " + either.either( String::toString, String::toString ) );
        } ) );


        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);



        Stream.range(0,10).foreachDoEffect( i -> {
            executor.execute( () -> one.next( i + "a" ) );
            executor.execute( () -> other.next( i + "b") );
        } );

        executor.awaitTermination( 10, TimeUnit.SECONDS );

    }

}
