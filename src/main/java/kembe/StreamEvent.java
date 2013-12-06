package kembe;

import fj.F;
import fj.P1;
import fj.Show;
import fj.data.Either;
import fj.data.NonEmptyList;
import fj.data.Validation;

public abstract class StreamEvent<A> {

    public static <A,B> F<Either<StreamEvent<A>,StreamEvent<B>>,StreamEvent<Either<A,B>>> normEither(){
        return new F<Either<StreamEvent<A>, StreamEvent<B>>, StreamEvent<Either<A, B>>>() {
            @Override public StreamEvent<Either<A, B>> f(Either<StreamEvent<A>, StreamEvent<B>> either) {
                if(either.isLeft())
                    return either.left().value().map(Either.<A,B>left_());
                else
                    return either.right().value().map(Either.<A,B>right_());
            }
        };
    }

    public static <A, B> F<StreamEvent<A>, StreamEvent<B>> lift(final F<A, B> f) {
        return new F<StreamEvent<A>, StreamEvent<B>>() {
            @Override
            public StreamEvent<B> f(StreamEvent<A> aStreamEvent) {
                return aStreamEvent.map( f );
            }
        };
    }

    public static <A, E, B> F<StreamEvent<A>, StreamEvent<B>> liftV(final F<A, Validation<NonEmptyList<E>, B>> f, final Show<E> msgConverter) {
        return new F<StreamEvent<A>, StreamEvent<B>>() {
            @Override public StreamEvent<B> f(StreamEvent<A> aStreamEvent) {
                return aStreamEvent
                        .fold(
                                new F<A, StreamEvent<B>>() {
                                    @Override public StreamEvent<B> f(A a) {
                                        Validation<NonEmptyList<E>, B> v = f.f( a );
                                        if (v.isSuccess())
                                            return StreamEvent.next( v.success() );
                                        else
                                            return StreamEvent.error( new Exception( Show.nonEmptyListShow( msgConverter ).showS( v.fail() ) ) );
                                    }
                                }, new F<Exception, StreamEvent<B>>() {
                                    @Override public StreamEvent<B> f(Exception e) {
                                        return StreamEvent.error( e );
                                    }
                                }, new P1<StreamEvent<B>>() {
                                    @Override public StreamEvent<B> _1() {
                                        return StreamEvent.done();
                                    }
                                }
                        );
            }
        };
    }

    public static <A> Next<A> next(A value) {
        return new Next<>( value );
    }

    public static <A> F<A, Next<A>> next() {
        return new F<A, Next<A>>() {
            @Override public Next<A> f(A a) {
                return next( a );
            }
        };
    }

    public static <A> Error<A> error(Exception e) {
        return new Error<>( e );
    }

    public static <A> Done<A> done() {
        return new Done<>();
    }

    public <E, B> StreamEvent<B> mapV(F<A, Validation<NonEmptyList<E>, B>> f,Show<E> msgConverter) {
        return liftV( f,msgConverter ).f( this );
    }

    public abstract void effect(EventStreamHandler handler);

    public abstract <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone);

    public abstract <B> StreamEvent<B> map(F<A, B> f);

    public static class Done<A> extends StreamEvent<A> {


        @Override
        public void effect(EventStreamHandler handler) {
            handler.done();
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
            return onDone._1();
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

        @Override
        public void effect(EventStreamHandler handler) {
            handler.error( e );
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
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

        @Override
        public void effect(EventStreamHandler handler) {
            handler.next( value );
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
            return onNext.f( value );
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Next<>( f.f( value ) );
        }
    }

}
