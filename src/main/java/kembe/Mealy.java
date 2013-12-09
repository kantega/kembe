package kembe;

import fj.F;

public abstract class Mealy<A,B> {

    public abstract Transition<A,B> apply(A a);


    public Mealy<A,B> self(){
        return this;
    }

    public <C> Mealy<A,C> map(F<B,C> f){
        return map(this,f);
    }

    public static class Transition<A,B>{
        public final B result;
        public final Mealy<A,B> nextMealy;

        Transition(B result, Mealy<A, B> nextMealy) {
            this.result = result;
            this.nextMealy = nextMealy;
        }
    }

    public static <A,B> Transition<A,B> transition(B value, Mealy<A,B> next){
        return new Transition<>(value,next);
    }

    public static <A,B,C> Mealy<A,C> map(final Mealy<A,B> mealy, final F<B,C> f){
        return new Mealy<A, C>() {
            @Override public Transition<A, C> apply(A a) {
                Transition<A,B> t = mealy.apply( a );
                return new Transition<>( f.f(t.result),map(t.nextMealy,f) );
            }
        };
    }

    public static <A,B> F<A,B> toUnpureF(Mealy<A,B> mealy){
        return new Driver<>( mealy );
    }

    /**
     * Sideffecting keeper of mealy state.
     * @param <A>
     * @param <B>
     */
    static class Driver<A,B> extends F<A,B>{

        private volatile Mealy<A,B> mealy;

        public Driver(final Mealy<A,B> mealy){
            this.mealy = mealy;
        }

        @Override public B f(A a) {
            Transition<A,B> t = mealy.apply( a );
            mealy = t.nextMealy;
            return t.result;
        }
    }

}
