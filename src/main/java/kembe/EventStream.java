package kembe;

import fj.*;
import fj.data.Either;
import fj.data.Option;
import fj.data.Stream;
import fj.data.Validation;
import fj.function.Effect1;
import kembe.stream.*;
import kembe.util.Functions;
import kembe.util.Shows;
import kembe.util.Split;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;


public abstract class EventStream<A> {


    /**
     * Creates an empty eventstream that calls done() on the subscriber at once.
     *
     * @param <A>
     * @return
     */
    public static <A> EventStream<A> nil() {
        return new EventStream<A>() {
            @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
                subscriber.done();
                return OpenEventStream.noOp( this );
            }
        };
    }

    public static <A> EventStream<A> error(final Exception e) {
        return new EventStream<A>() {
            @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
                subscriber.error( e );
                subscriber.done();
                return OpenEventStream.noOp( this );
            }
        };
    }

    public static <A> EventStream<A> one(A value) {
        return values( value );
    }

    public static <A> EventStream values(A... values) {
        return fromIterable( Arrays.<A>asList( values ) );
    }

    public static <A> EventStream<A> fromStream(final Stream<A> stream) {
        return new EventStream<A>() {
            @Override
            public OpenEventStream<A> open(final EventStreamSubscriber<A> effect) {

                stream.foreachDoEffect( a -> effect.e( StreamEvent.next( a ) ) );
                effect.e( StreamEvent.<A>done() );

                return OpenEventStream.noOp( this );
            }
        };
    }

    public static <A> EventStream<A> fromIterable(final Iterable<A> iterable) {
        return fromIterator( iterable.iterator() );
    }

    public static <A> EventStream<A> fromIterator(final Iterator<A> iterator) {
        return new EventStream<A>() {
            @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
                try {
                    while (iterator.hasNext()) {
                        subscriber.next( iterator.next() );
                    }
                } catch (Exception e) {
                    subscriber.error( e );
                }
                subscriber.done();

                return OpenEventStream.noOp( this );
            }
        };
    }

    public static <A> F<Stream<A>, EventStream<A>> fromStream() {
        return EventStream::fromStream;
    }

    public static <A> F<A, EventStream<A>> fromStream(F<A, Stream<A>> f) {
        return Function.andThen( f, EventStream.<A>fromStream() );
    }

    public static <A> EventStream<A> normalize(EventStream<Either<A, A>> eitherStream) {
        return eitherStream.map( EitherEventStream.<A>normalize() );
    }

    public static <A> F<Either<A, A>, A> normalize() {
        return Either::reduce;
    }

    public static <E, A> F<EventStream<Validation<E, A>>, EventStream<A>> normalizeValidation(final Show<E> show) {
        return new F<EventStream<Validation<E, A>>, EventStream<A>>() {
            @Override public EventStream<A> f(EventStream<Validation<E, A>> validationEventStream) {
                return new RawMappedEventStream<>( validationEventStream, new F<StreamEvent<Validation<E, A>>, StreamEvent<A>>() {
                    @Override public StreamEvent<A> f(StreamEvent<Validation<E, A>> validationStreamEvent) {
                        return validationStreamEvent.fold(
                                new F<Validation<E, A>, StreamEvent<A>>() {
                                    @Override public StreamEvent<A> f(Validation<E, A> as) {
                                        if (as.isSuccess())
                                            return StreamEvent.next( as.success() );
                                        else
                                            return StreamEvent.error( new Exception( show.showS( as.fail() ) ) );
                                    }
                                }, new F<Exception, StreamEvent<A>>() {
                                    @Override public StreamEvent<A> f(Exception e) {
                                        return StreamEvent.error( e );
                                    }
                                }, new Supplier<StreamEvent<A>>() {
                                    @Override public StreamEvent<A> get() {
                                        return StreamEvent.done();
                                    }
                                }
                        );
                    }
                } );
            }
        };
    }

    public static <A, B> Split<A, B> split(final EventStream<Either<A, B>> eitherStream) {
        EventStream<A> as = eitherStream
                .map( Functions.<A, B>left() )
                .filter( Functions.<A>isSome() )
                .map( Functions.<A>getSome() );

        EventStream<B> bs = eitherStream
                .map( Functions.<A, B>right() )
                .filter( Functions.<B>isSome() )
                .map( Functions.<B>getSome() );

        return new Split( as, bs );
    }

    public static <A> F<EventStream<A>, OpenEventStream<A>> open_(final EventStreamSubscriber<A> handler) {
        return stream -> stream.open( handler );
    }

    public static <A, B> F<EventStream<A>, EventStream<B>> lift(final F<A, B> f) {
        return stream -> stream.map( f );
    }

    public static <E, T> EventStream<T> validation(EventStream<Validation<E, T>> validatingEventStream, Show<E> show) {
        return new ValidationMappedEventStream<E, T>( validatingEventStream, show );
    }

    public static <E, T> F<EventStream<Validation<E, T>>, EventStream<T>> validation(final Show<E> show) {
        return stream -> validation( stream, show );
    }

    public static <A, B> EitherEventStream<A, B> or(EventStream<A> a, EventStream<B> b) {
        return new EitherEventStream<>( a, b );
    }

    public static <A> EventStream<A> merge(EventStream<A> one, EventStream<A> other) {
        return EventStream.normalize( new EitherEventStream<>( one, other ) );
    }

    public static <A> EventStream<A> flatten(EventStream<EventStream<A>> as) {
        return new FlatteningEventStream<>( as );
    }

    public static <A, B> MealyEventStream<A, B> mapStateful(EventStream<A> as, Mealy<A, B> f) {
        return new MealyEventStream<>( as, f );
    }

    public static <A, B> EventStream<B> bindStateful(EventStream<A> as, Mealy<A, EventStream<B>> s) {
        return flatten( mapStateful( as, s ) );
    }

    public static <A> EventStream<A> tap(EventStream<A> as, EventStreamSubscriber<A> effect) {
        return new TapEventStream<>( as, effect );
    }

    public static <A> EventStreamFork<A> fork(EventStream<A> stream) {
        return new EventStreamFork<A>( stream );
    }

    public abstract OpenEventStream<A> open(EventStreamSubscriber<A> subscriber);

    public EventStream<A> filter(final F<A, Boolean> pred) {
        return new FilterEventStream<>( this, pred );
    }

    public <B> EventStream<B> map(final F<A, B> f) {
        return new MappedEventStream<>( this, f );
    }

    public <B> EventStream<B> mapV(final F<A, Validation<Exception, B>> f) {
        return EventStream.validation( this.map( f ), Shows.exceptionShow );
    }


    public <B> EventStream<B> mapO(final F<A, Option<B>> f) {
        return new FlattenIterableEventStream<>( this, Function.andThen( f, bs -> bs ) );
    }

    public <B> EventStream<B> mapStateful(final Mealy<A, B> stateMachine) {
        return EventStream.mapStateful( this, stateMachine );
    }

    public <B> EventStream<B> mapStatefulO(final Mealy<A, Option<B>> stateMachine) {
        return bindStateful( this, stateMachine.map( EventStream::fromIterable ) );
    }

    public <B> EventStream<B> mapEvent(final F<StreamEvent<A>, StreamEvent<B>> f) {
        return new RawMappedEventStream<>( this, f );
    }

    public <B> EventStream<B> bind(F<A, EventStream<B>> f) {
        return flatten( this.map( f ) );
    }

    public <B> EventStream<B> bindEvent(F<StreamEvent<A>, EventStream<B>> f) {
        return new RawBoundEventStream( this, f );
    }

    public <B> EventStream<B> bindStateful(final Mealy<A, EventStream<B>> stateMachine) {
        return bindStateful( this, stateMachine );
    }

    public <B> EventStream<B> bindEventStateful(final Mealy<StreamEvent<A>, EventStream<B>> f) {
        return new FlattenRawIterableStatefulEventStream<>( this, f );
    }

    public <B> EventStream<Either<A, B>> or(EventStream<B> other) {
        return or( this, other );
    }

    public EventStream<A> merge(EventStream<A> other) {
        return EventStream.merge( this, other );
    }

    /**
     * Opens first the argument eventstream, then this eventstream. Results from the other are buffered, and
     * forwarded when this eventstream is done
     *
     * @param eventual
     * @return
     */
    public EventStream<A> andThen(EventStream<A> eventual) {
        return new ParallellBufferedAndThenEventStream<>( this, eventual );
    }

    public EventStream<A> append(EventStream<A> other) {
        return new AppendEventStream<A>( this, other );
    }

    public EventStream<A> tap(EventStreamSubscriber<A> effect) {
        return tap( this, effect );
    }

    public EventStreamFork<A> fork() {
        return EventStream.fork( this );
    }

}
