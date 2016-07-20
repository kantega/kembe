package kembe;

import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Asserts {

    public static void awaitAssert(long timeout, TimeUnit timeUnit, CountDownLatch... latches) throws InterruptedException {

        for (CountDownLatch latch : latches) {
            latch.await( timeout, timeUnit );
        }
        for (CountDownLatch latch : latches) {
            Assert.assertTrue( latch.getCount() == 0 );
        }


    }

}
