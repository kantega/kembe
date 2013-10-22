package org.kantega.falls;

public interface EventSource<A> {

    public EventStream<A> newStream();

}
