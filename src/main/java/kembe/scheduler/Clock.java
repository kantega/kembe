package kembe.scheduler;

import kembe.EventStream;
import kembe.EventStreamSubscriber;
import kembe.OpenEventStream;
import kembe.StreamEvent;
import org.joda.time.Instant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Clock {

    public static EventStream<Instant> seconds() {
        return ticksEvery( 1, TimeUnit.SECONDS );
    }

    public static EventStream<Instant> everyHundredMillis() {
        return ticksEvery( 10, TimeUnit.MILLISECONDS );
    }

    public static EventStream<Instant> everyTenMillis() {
        return ticksEvery( 10, TimeUnit.MILLISECONDS );
    }

    public static EventStream<Instant> ticksEvery(final int interval, final TimeUnit timeUnit) {
        return new EventStream<Instant>() {
            @Override public OpenEventStream<Instant> open(final EventStreamSubscriber<Instant> effect) {
                final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

                Runnable ticker = new Runnable() {
                    @Override public void run() {
                        effect.e( StreamEvent.next( new Instant( System.currentTimeMillis() ) ) );
                    }
                };
                ses.scheduleAtFixedRate( ticker, 0, interval, timeUnit );
                final EventStream<Instant> self = this;

                return new OpenEventStream<Instant>() {
                    @Override public EventStream<Instant> close() {
                        ses.shutdownNow();
                        return self;
                    }
                };
            }
        };
    }

}
