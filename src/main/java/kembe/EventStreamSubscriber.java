package kembe;

import fj.Effect;
import fj.F;
import fj.Unit;

public abstract class EventStreamSubscriber<A> {

    public static <A> EventStreamSubscriber<A> create(final EventStreamHandler<A> handler) {
        return new EventStreamSubscriber<A>() {
            @Override public void e(StreamEvent<A> aStreamEvent) {
                aStreamEvent.effect( handler );
            }
        };
    }

    public static <A> EventStreamSubscriber<A> create(final Effect<StreamEvent<A>> handler) {
        return new EventStreamSubscriber<A>() {
            @Override public void e(StreamEvent<A> aStreamEvent) {
                handler.e( aStreamEvent );
            }
        };
    }

    public <B> EventStreamSubscriber<B> onNext(final Effect<B> effect) {
        final EventStreamSubscriber<A> self = this;
        return create( new EventStreamHandler<B>() {
            @Override public void next(B o) {
                effect.e( o );
            }

            @Override public void error(Exception e) {
                self.error( e );
            }

            @Override public void done() {
                self.done();
            }
        } );
    }

    public EventStreamSubscriber<A> onError(final Effect<Exception> effect) {
        final EventStreamSubscriber<A> self = this;
        return create( new EventStreamHandler<A>() {
            @Override public void next(A o) {
                self.next( o );
            }

            @Override public void error(Exception e) {
                effect.e( e );
            }

            @Override public void done() {
                self.done();
            }
        } );
    }

    public EventStreamSubscriber<A> onDone(final Effect<Unit> effect) {
        final EventStreamSubscriber<A> self = this;
        return create( new EventStreamHandler<A>() {
            @Override public void next(A o) {
                self.next( o );
            }

            @Override public void error(Exception e) {
                self.error( e );
            }

            @Override public void done() {
                effect.e( Unit.unit() );
            }
        } );
    }

    public void next(A a) {
        try {
            e( StreamEvent.next( a ) );
        } catch (Exception e) {
            error( e );
        }
    }

    public void error(Exception e) {
        e( StreamEvent.<A>error( e ) );
    }

    public void done() {
        e( StreamEvent.<A>done() );
    }

    public <B> EventStreamSubscriber<B> comap(final F<B, A> f) {
        return new EventStreamSubscriber<B>() {
            @Override public void e(StreamEvent<B> event) {
                EventStreamSubscriber.this.e( event.map( f ) );
            }
        };
    }

    public <B> EventStreamSubscriber<B> comapEvent(final F<StreamEvent<B>, StreamEvent<A>> f) {
        return new EventStreamSubscriber<B>() {
            @Override public void e(StreamEvent<B> event) {
                EventStreamSubscriber.this.e( f.f( event ) );
            }
        };
    }

    public Effect<StreamEvent<A>> toEffect() {
        return new Effect<StreamEvent<A>>() {
            @Override public void e(StreamEvent<A> aStreamEvent) {
                EventStreamSubscriber.this.e( aStreamEvent );
            }
        };
    }

    public abstract void e(StreamEvent<A> event);

}
