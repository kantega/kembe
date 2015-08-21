package kembe;

import fj.data.List;
import fj.function.Effect0;
import fj.function.Effect1;

public abstract class OpenEventStream<A> {

    public static <A> Effect1<OpenEventStream<A>> closeE() {
        return OpenEventStream<A>::close;
    }

    public static <A> OpenEventStream<A> noOp(final EventStream<A> current) {
        return wrap( current, List.<OpenEventStream<?>>nil() );
    }

    public static <A> OpenEventStream<A> wrap(final EventStream<A> current, final OpenEventStream<?> prev) {
        return wrap( current, List.<OpenEventStream<?>>single( prev ) );
    }

    public static <A> OpenEventStream<A> wrap(final EventStream<A> current, final List<OpenEventStream<?>> prevs) {
        return new OpenEventStream<A>() {
            @Override
            public EventStream<A> close() {
                for (OpenEventStream<?> prev : prevs) {
                    prev.close();
                }
                return current;
            }
        };
    }

    public static <A> OpenEventStream<A> onClose(Effect0 close,EventStream<A> next){
        return new OpenEventStream<A>() {
            @Override public EventStream<A> close() {
                close.f();
                return next;
            }
        };
    }

    public abstract EventStream<A> close();

}
