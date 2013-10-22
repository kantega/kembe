package org.kantega.falls;

public interface EventStreamHandler<A>
{

    public void next(A a);

    public void error(Exception e);

    public void done();

}
