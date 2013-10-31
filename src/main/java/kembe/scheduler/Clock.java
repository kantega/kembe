package kembe.scheduler;

import fj.Effect;
import org.joda.time.Instant;
import kembe.EventStream;
import kembe.OpenEventStream;
import kembe.StreamEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Clock {

    public static EventStream<Instant> seconds() {
        return ticksEvery( 1, TimeUnit.SECONDS );
    }

    public static EventStream<Instant> tenMillis() {
        return ticksEvery( 10, TimeUnit.MILLISECONDS );
    }

    public static EventStream<Instant> ticksEvery(final int interval, final TimeUnit timeUnit) {
        return new EventStream<Instant>() {
            @Override public OpenEventStream<Instant> open(final Effect<StreamEvent<Instant>> effect) {
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
