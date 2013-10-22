package org.kantega.falls;

import fj.*;
import fj.data.Either;
import fj.data.Stream;
import fj.data.Validation;
import org.kantega.falls.pipes.*;

public abstract class EventStream<A>
{
    public abstract OpenEventStream<A> open(Effect<StreamEvent<A>> effect);

    public static <A> EventStream<A> fromStream(final Stream<A> stream)
    {
        return new EventStream<A>()
        {
            @Override
            public OpenEventStream<A> open(final Effect<StreamEvent<A>> effect)
            {

                stream.foreach(new Effect<A>()
                {
                    @Override
                    public void e(A a)
                    {
                        effect.e(StreamEvent.next(a));
                    }
                });
                effect.e(StreamEvent.<A>done());

                return OpenEventStream.noOp(this);
            }
        };
    }

    public static <A> EventStream<A> normalize(EventStream<Either<A, A>> eitherStream)
    {
        return eitherStream.map(EitherEventStream.<A>normalize());
    }

    public static <A> F<Either<A, A>, A> normalize()
    {
        return new F<Either<A, A>, A>()
        {
            @Override
            public A f(Either<A, A> either)
            {
                return either.either(Function.<A>identity(), Function.<A>identity());
            }
        };
    }

    public static <A, B> P2<EventStream<A>, EventStream<B>> split(final EventStream<Either<A, B>> eitherStream)
    {
        EventStream<A> as = eitherStream
                .map(Functions.<A, B>left())
                .filter(Functions.<A>isSome())
                .map(Functions.<A>getSome());

        EventStream<B> bs = eitherStream
                .map(Functions.<A, B>right())
                .filter(Functions.<B>isSome())
                .map(Functions.<B>getSome());

        return P.p(as, bs);
    }

    public static <A> F<EventStream<A>, OpenEventStream<A>> open_(final Effect<StreamEvent<A>> handler)
    {
        return new F<EventStream<A>, OpenEventStream<A>>()
        {
            @Override
            public OpenEventStream<A> f(EventStream<A> stream)
            {
                return stream.open(handler);
            }
        };
    }

    public static <A, B> F<EventStream<A>, EventStream<B>> lift(final F<A, B> f)
    {
        return new F<EventStream<A>, EventStream<B>>()
        {
            @Override
            public EventStream<B> f(EventStream<A> stream)
            {
                return stream.map(f);
            }
        };
    }

    public static <E, T> EventStream<T> validation(EventStream<Validation<E, T>> validatingEventStream, Show<E> show)
    {
        return new ValidationMappedEventStream<E, T>(validatingEventStream, show);
    }

    public static <E, T> F<EventStream<Validation<E, T>>, EventStream<T>> validation(final Show<E> show)
    {
        return new F<EventStream<Validation<E, T>>, EventStream<T>>()
        {
            @Override
            public EventStream<T> f(EventStream<Validation<E, T>> stream)
            {
                return validation(stream, show);
            }
        };
    }

    public FilterEventStream<A> filter(final F<A, Boolean> pred)
    {
        return new FilterEventStream<A>(this, pred);
    }

    public <B> MappedEventStream<A, B> map(final F<A, B> f)
    {
        return new MappedEventStream<A, B>(this, f);
    }

    public <B> BoundEventStream<A, B> bind(F<A, EventStream<B>> f)
    {
        return new BoundEventStream<A, B>(this, f);
    }

    public <B> EitherEventStream<A, B> or(EventStream<B> other)
    {
        return or(this, other);
    }

    public static <A, B> EitherEventStream<A, B> or(EventStream<A> a, EventStream<B> b)
    {
        return new EitherEventStream<A, B>(a, b);
    }

    public EventStream<A> merge(EventStream<A> other)
    {
        return EventStream.normalize(new EitherEventStream<A, A>(this, other));
    }

    public AndThenEventStream<A> andThen(EventStream<A> eventual)
    {
        return new AndThenEventStream<A>(this, eventual);
    }

    public <B> JoinEventStream<A, B> join(EventStream<B> other)
    {
        return new JoinEventStream<A, B>(this, other);
    }
}
