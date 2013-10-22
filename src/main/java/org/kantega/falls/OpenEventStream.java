package org.kantega.falls;

import fj.data.List;

public abstract class OpenEventStream<A> {

    public static <A> OpenEventStream<A> wrap( final EventStream<A> current, final OpenEventStream<?> prev){
        return wrap(current,List.<OpenEventStream<?>>single(prev));
    }

    public static <A> OpenEventStream<A> wrap(final EventStream<A> current, final List<OpenEventStream<?>> prevs){
        return new OpenEventStream<A>() {
            @Override
            public EventStream<A> close() {
               for(OpenEventStream<?> prev:prevs){
                   prev.close();
               }
                return current;
            }
        };
    }


    public abstract EventStream<A> close();

}
