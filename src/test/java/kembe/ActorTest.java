package kembe;

import fj.Unit;
import fj.control.parallel.Actor;
import fj.control.parallel.Strategy;
import kembe.util.Actors;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ActorTest {

    @Test
    public void loadTestStackSafeQueueActor() throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool( 3 );
        final AtomicLong receivedCounter = new AtomicLong( 0 );
        final AtomicLong sentCounter = new AtomicLong( 0 );
        final long n = 10000;
        final CountDownLatch latch = new CountDownLatch( 1 );

        final Actor<String> a = Actors.stackSafeQueueActor( Strategy.<Unit>executorStrategy( Executors.newSingleThreadExecutor() ), s -> {
            if (receivedCounter.incrementAndGet() == n)
                latch.countDown();

            if(receivedCounter.get()==10)
                throw new Error("Lets seee");

            try {
                Thread.sleep( 2 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } );


        Runnable r = () -> {
            while (sentCounter.getAndIncrement() < n)
                a.act( "jalla" );

        };

        executor.submit( r );
        executor.submit( r );
        executor.submit( r );


        latch.await( 30, TimeUnit.SECONDS );
        Assert.assertEquals( n,receivedCounter.get() );
    }

}
