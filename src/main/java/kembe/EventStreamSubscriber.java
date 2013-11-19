package kembe;

import fj.Effect;
import fj.F;

public class EventStreamSubscriber<A> extends Effect<StreamEvent<A>> {
    final Effect<StreamEvent.Next<A>> onNext;

    final Effect<StreamEvent.Error<A>> onException;

    final Effect<StreamEvent.Done<A>> onDone;

    protected EventStreamSubscriber(
            Effect<StreamEvent.Next<A>> onNext,
            Effect<StreamEvent.Error<A>> onException,
            Effect<StreamEvent.Done<A>> onDone) {
        this.onNext = onNext;
        this.onException = onException;
        this.onDone = onDone;
    }

    public static <A> EventStreamSubscriber<A> forwardTo(Effect<StreamEvent<A>> effect) {
        return EventStreamSubscriber.create(
                EventStreamSubscriber.<A>forwardNext( effect ),
                EventStreamSubscriber.<A, A>forwardError( effect ),
                EventStreamSubscriber.<A, A>forwardDone( effect )
        );
    }

    public static <A> EventStreamSubscriber<A> create(
            final Effect<StreamEvent.Next<A>> onNext,
            final Effect<StreamEvent.Error<A>> onException,
            final Effect<StreamEvent.Done<A>> onDone) {
        return new EventStreamSubscriber<A>( onNext, onException, onDone );
    }

    public static <A> EventStreamSubscriber<A> create(final EventStreamHandler<A> handler) {
        return create(
                new Effect<StreamEvent.Next<A>>() {
                    public void e(StreamEvent.Next<A> next) {
                        handler.next( next.value );
                    }
                },
                new Effect<StreamEvent.Error<A>>() {
                    public void e(StreamEvent.Error<A> error) {
                        handler.error( error.e );
                    }
                },
                new Effect<StreamEvent.Done<A>>() {
                    public void e(StreamEvent.Done<A> done) {
                        handler.done();
                    }
                }
        );
    }

    public static <A> Effect<StreamEvent.Next<A>> forwardNext(final Effect<StreamEvent<A>> effect) {
        return new Effect<StreamEvent.Next<A>>() {
            @Override
            public void e(StreamEvent.Next<A> aNext) {
                effect.e( aNext );
            }
        };
    }

    public static <A, B> Effect<StreamEvent.Error<A>> forwardError(final Effect<StreamEvent<B>> effect) {
        return new Effect<StreamEvent.Error<A>>() {
            @Override
            public void e(StreamEvent.Error<A> error) {
                effect.e( StreamEvent.<B>error( error.e ) );
            }
        };
    }

    public static <A, B> Effect<StreamEvent.Done<A>> forwardDone(final Effect<StreamEvent<B>> effect) {
        return new Effect<StreamEvent.Done<A>>() {
            @Override
            public void e(StreamEvent.Done<A> next) {
                effect.e( StreamEvent.<B>done() );
            }
        };
    }

    public static <A> Effect<StreamEvent.Done<A>> noOpDone() {
        return new Effect<StreamEvent.Done<A>>() {
            @Override
            public void e(StreamEvent.Done<A> next) {
            }
        };
    }

    private static <A, B> Effect<StreamEvent.Error<B>> castError(Effect<StreamEvent.Error<A>> e) {
        return e.comap( new F<StreamEvent.Error<B>, StreamEvent.Error<A>>() {
            @Override public StreamEvent.Error<A> f(StreamEvent.Error<B> bError) {
                return StreamEvent.error( bError.e );
            }
        } );
    }

    private static <A, B> Effect<StreamEvent.Done<B>> castDone(Effect<StreamEvent.Done<A>> e) {
        return e.comap( new F<StreamEvent.Done<B>, StreamEvent.Done<A>>() {
            @Override public StreamEvent.Done<A> f(StreamEvent.Done<B> bDone) {
                return StreamEvent.done();
            }
        } );
    }

    public <B> EventStreamSubscriber<B> onNext(final Effect<StreamEvent.Next<B>> onNext) {
        return EventStreamSubscriber.create(
                onNext,
                EventStreamSubscriber.<A, B>castError( this.onException ),
                EventStreamSubscriber.<A, B>castDone( this.onDone ) );
    }

    public EventStreamSubscriber<A> onDone(final Effect<StreamEvent.Done<A>> onDone) {
        return EventStreamSubscriber.create( this.onNext, this.onException, onDone );
    }

    @Override
    public void e(StreamEvent<A> aStreamEvent) {
        aStreamEvent
                .effect(
                        new Effect<StreamEvent.Next<A>>() {
                            @Override
                            public void e(StreamEvent.Next<A> aNext) {
                                onNext.e( aNext );
                            }
                        }, new Effect<StreamEvent.Error<A>>() {
                            @Override
                            public void e(StreamEvent.Error<A> err) {
                                onException.e( err );
                            }
                        }, new Effect<StreamEvent.Done<A>>() {
                            @Override
                            public void e(StreamEvent.Done<A> done) {
                                onDone.e( done );
                            }
                        }
                );
    }
}
