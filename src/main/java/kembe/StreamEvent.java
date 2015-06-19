package kembe;

import fj.F;
import fj.Show;
import fj.data.Either;
import fj.data.NonEmptyList;
import fj.data.Validation;
import fj.function.Effect1;

import java.util.function.Supplier;

public abstract class StreamEvent<A> {

    public static <A, B> F<Either<StreamEvent<A>, StreamEvent<B>>, StreamEvent<Either<A, B>>> normEither() {
        return either -> {
            if (either.isLeft())
                return either.left().value().map( Either.<A, B>left_() );
            else
                return either.right().value().map( Either.<A, B>right_() );
        };
    }

    public static <A, B> F<StreamEvent<A>, StreamEvent<B>> lift(final F<A, B> f) {
        return aStreamEvent -> aStreamEvent.map( f );
    }

    public static <A, E, B> F<StreamEvent<A>, StreamEvent<B>> liftV(final F<A, Validation<NonEmptyList<E>, B>> f, final Show<E> msgConverter) {
        return aStreamEvent -> aStreamEvent
                .fold(
                        a -> {
                            Validation<NonEmptyList<E>, B> v = f.f( a );
                            if (v.isSuccess())
                                return StreamEvent.next( v.success() );
                            else
                                return StreamEvent.error( new Exception( Show.nonEmptyListShow( msgConverter ).showS( v.fail() ) ) );
                        },
                        StreamEvent::error,
                        (Supplier<StreamEvent<B>>) StreamEvent::done
                );
    }

    public static <A> Next<A> next(A value) {
        return new Next<>( value );
    }

    public static <A> F<A, Next<A>> next() {
        return StreamEvent::next;
    }

    public static <A> Error<A> error(Exception e) {
        return new Error<>( e );
    }

    public static <A> Done<A> done() {
        return new Done<>();
    }

    public <E, B> StreamEvent<B> mapV(F<A, Validation<NonEmptyList<E>, B>> f, Show<E> msgConverter) {
        return liftV( f, msgConverter ).f( this );
    }

    public void effect(final EventStreamHandler handler) {
        effect( aNext -> handler.next( aNext.value ), aError -> handler.error( aError.e ), aDone -> handler.done() );
    }

    public abstract void effect(Effect1<Next<A>> onNext, Effect1<Error<A>> onError, Effect1<Done<A>> onDone);

    public abstract <T> T fold(F<A, T> onNext, F<Exception, T> onError, Supplier<T> onDone);

    public abstract <B> StreamEvent<B> map(F<A, B> f);

    public static class Done<A> extends StreamEvent<A> {


        @Override public void effect(Effect1<Next<A>> onNext, Effect1<Error<A>> onError, Effect1<Done<A>> onDone) {
            onDone.f( this );
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, Supplier<T> onDone) {
            return onDone.get();
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Done<>();
        }
    }

    public static class Error<A> extends StreamEvent<A> {
        public final Exception e;

        public Error(Exception e) {
            this.e = e;
        }

        @Override public void effect(Effect1<Next<A>> onNext, Effect1<Error<A>> onError, Effect1<Done<A>> onDone) {
            onError.f( this );
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, Supplier<T> onDone) {
            return onError.f( e );
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Error<>( e );
        }
    }

    public static class Next<A> extends StreamEvent<A> {

        public final A value;


        public Next(A value) {
            this.value = value;
        }

        @Override public void effect(Effect1<Next<A>> onNext, Effect1<Error<A>> onError, Effect1<Done<A>> onDone) {
            try {
                onNext.f( this );
            } catch (Throwable t) {
                onError.f( StreamEvent.<A>error( new Exception( "Exception thrown while calling onNext: " + t.getMessage(), t ) ) );
            }
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, Supplier<T> onDone) {
            return onNext.f( value );
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            try {
                B nextValue = f.f( value );
                return new Next<>( nextValue );
            } catch (Exception e) {
                return StreamEvent.error( e );
            }

        }
    }

}
