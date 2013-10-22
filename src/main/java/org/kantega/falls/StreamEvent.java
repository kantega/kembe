package org.kantega.falls;

import fj.Effect;
import fj.F;
import fj.P1;

public abstract class StreamEvent<A> {

    public static <A, B> F<StreamEvent<A>, StreamEvent<B>> lift(final F<A, B> f) {
        return new F<StreamEvent<A>, StreamEvent<B>>() {
            @Override
            public StreamEvent<B> f(StreamEvent<A> aStreamEvent) {
                return aStreamEvent.map(f);
            }
        };
    }

    public static <A> Next<A> next(A value){
        return new Next<A>(value);
    }

    public static <A> Error<A> error(Exception e){
        return new Error<A>(e);
    }

    public static <A> Done<A> done(){
        return new Done<A>();
    }
    public abstract void effect(Effect<Next<A>> onNext, Effect<Error<A>> onException, Effect<Done<A>> onDone);

    public abstract <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone);

    public abstract <B> StreamEvent<B> map(F<A, B> f);

    public static class Done<A> extends StreamEvent<A> {


        @Override
        public void effect(Effect<Next<A>> onNext, Effect<Error<A>> onException, Effect<Done<A>> onDone) {
            onDone.e(this);
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
            return onDone._1();
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Done<B>();
        }
    }

    public static class Error<A> extends StreamEvent<A> {
        public final Exception e;

        public Error(Exception e) {
            this.e = e;
        }

        @Override
        public void effect(Effect<Next<A>> onNext, Effect<Error<A>> onException, Effect<Done<A>> onDone) {
            onException.e(this);
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
            return onError.f(e);
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Error<B>(e);
        }
    }

    public static class Next<A> extends StreamEvent<A> {

        public final A value;


        public Next(A value) {
            this.value = value;
        }

        @Override
        public void effect(Effect<Next<A>> onNext, Effect<Error<A>> onException, Effect<Done<A>> onDone) {
            onNext.e(this);
        }

        @Override
        public <T> T fold(F<A, T> onNext, F<Exception, T> onError, P1<T> onDone) {
            return onNext.f(value);
        }

        @Override
        public <B> StreamEvent<B> map(F<A, B> f) {
            return new Next<B>(f.f(value));
        }
    }

}
