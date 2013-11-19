package kembe;

import fj.Effect;
import fj.F;
import fj.P1;
import fj.Show;
import fj.data.*;
import kembe.stream.*;

public abstract class EventStream<A> {

    public static <A> EventStream<A> fromStream(final Stream<A> stream) {
        return new EventStream<A>() {
            @Override
            public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect) {

                stream.foreach( new Effect<A>() {
                    @Override
                    public void e(A a) {
                        effect.e( StreamEvent.next( a ) );
                    }
                } );
                effect.e( StreamEvent.<A>done() );

                return OpenEventStream.noOp( this );
            }
        };
    }

    public static <A> EventStream<A> normalize(EventStream<Either<A, A>> eitherStream) {
        return eitherStream.map( EitherEventStream.<A>normalize() );
    }

    public static <A> F<Either<A, A>, A> normalize() {
        return new F<Either<A, A>, A>() {
            @Override
            public A f(Either<A, A> either) {
                return Either.reduce( either );
            }
        };
    }

    public static <E, A> F<EventStream<Validation<E, A>>, EventStream<A>> normalizeValidation(final Show<E> show) {
        return new F<EventStream<Validation<E, A>>, EventStream<A>>() {
            @Override public EventStream<A> f(EventStream<Validation<E, A>> validationEventStream) {
                return new RawMappedEventStream<Validation<E, A>, A>( validationEventStream, new F<StreamEvent<Validation<E, A>>, StreamEvent<A>>() {
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
                                }, new P1<StreamEvent<A>>() {
                                    @Override public StreamEvent<A> _1() {
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

    public static <A> F<EventStream<A>, OpenEventStream<A>> open_(final Effect<StreamEvent<A>> handler) {
        return new F<EventStream<A>, OpenEventStream<A>>() {
            @Override
            public OpenEventStream<A> f(EventStream<A> stream) {
                return stream.open( handler );
            }
        };
    }

    public static <A, B> F<EventStream<A>, EventStream<B>> lift(final F<A, B> f) {
        return new F<EventStream<A>, EventStream<B>>() {
            @Override
            public EventStream<B> f(EventStream<A> stream) {
                return stream.map( f );
            }
        };
    }

    public static <E, T> EventStream<T> validation(EventStream<Validation<E, T>> validatingEventStream, Show<E> show) {
        return new ValidationMappedEventStream<E, T>( validatingEventStream, show );
    }

    public static <E, T> F<EventStream<Validation<E, T>>, EventStream<T>> validation(final Show<E> show) {
        return new F<EventStream<Validation<E, T>>, EventStream<T>>() {
            @Override
            public EventStream<T> f(EventStream<Validation<E, T>> stream) {
                return validation( stream, show );
            }
        };
    }

    public static <A, B> EitherEventStream<A, B> or(EventStream<A> a, EventStream<B> b) {
        return new EitherEventStream<A, B>( a, b );
    }

    public static <A> EventStream<A> merge(EventStream<A> one, EventStream<A> other) {
        return EventStream.normalize( new EitherEventStream<A, A>( one, other ) );
    }

    public static <A, B> OptionNormalizingEventStream<B> mapOption(EventStream<A> as, F<A, Option<B>> f) {
        return normalizeOption( as.map( f ) );
    }

    public static <A, B> ListNormalizingEventStream<B> mapList(EventStream<A> as, F<A, List<B>> f) {
        return normalizeList( as.map( f ) );
    }

    public static <A> OptionNormalizingEventStream<A> normalizeOption(EventStream<Option<A>> as) {
        return new OptionNormalizingEventStream<>( as );
    }

    public static <A> ListNormalizingEventStream<A> normalizeList(EventStream<List<A>> as) {
        return new ListNormalizingEventStream<>( as );
    }

    public static <A, B> MealyEventStream<A, B> mapStateful(EventStream<A> as, State<A, B> f) {
        return new MealyEventStream<>( as, f );
    }

    public static <A, B> EventStream<B> mapOptionalStateful(EventStream<A> as, State<A, Option<B>> f) {
        return normalizeOption( mapStateful( as, f ) );
    }

    public static <A, B> EventStream<B> mapListStateful(EventStream<A> as, State<A, List<B>> f) {
        return normalizeList( mapStateful( as, f ) );
    }

    public abstract OpenEventStream<A> open(Effect<StreamEvent<A>> effect);

    public FilterEventStream<A> filter(final F<A, Boolean> pred) {
        return new FilterEventStream<>( this, pred );
    }

    public <B> MappedEventStream<A, B> map(final F<A, B> f) {
        return new MappedEventStream<>( this, f );
    }

    public <B> OptionNormalizingEventStream<B> mapOption(final F<A, Option<B>> f) {
        return EventStream.mapOption( this, f );
    }

    public <B> OptionNormalizingEventStream<B> mapList(final F<A, List<B>> f) {
        return EventStream.mapOption( this, f );
    }

    public <B> MealyEventStream<A, B> mapStateful(final State<A, B> f) {
        return EventStream.mapStateful( this, f );
    }

    public <B> EventStream<B> mapOptionalStateful(final State<A, Option<B>> f) {
        return EventStream.mapOptionalStateful( this, f );
    }

    public <B> EventStream<B> mapListStateful(final State<A, List<B>> f) {
        return EventStream.mapListStateful( this, f );
    }

    public <B> RawMappedEventStream<A, B> rawMap(final F<StreamEvent<A>, StreamEvent<B>> f) {
        return new RawMappedEventStream<A, B>( this, f );
    }

    public <B> BoundEventStream<A, B> bind(F<A, EventStream<B>> f) {
        return new BoundEventStream<A, B>( this, f );
    }

    public <B> EitherEventStream<A, B> or(EventStream<B> other) {
        return or( this, other );
    }

    public EventStream<A> merge(EventStream<A> other) {
        return EventStream.merge( this, other );
    }

    public AndThenEventStream<A> andThen(EventStream<A> eventual) {
        return new AndThenEventStream<A>( this, eventual );
    }


}
