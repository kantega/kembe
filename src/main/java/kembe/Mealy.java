package kembe;

import fj.F;
import fj.F2;
import fj.P;
import fj.P2;
import fj.data.Stream;

public abstract class Mealy<A,B> {

    public abstract Transition<A,B> apply(A a);


    public Mealy<A,B> self(){
        return this;
    }

    public <C> Mealy<A,C> map(F<B,C> f){
        return map(this,f);
    }


    public F<A,B> toUnsafeIncrementingFunction(){
        return new UnsafeIncrementingFunction<>( this );
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

    public static <A,B> P2<Mealy<A,B>,Stream<B>> foldInit(Mealy<A,B> mealy){
        return P.p( mealy, Stream.<B>nil() );
    }

    public static <A,B> F2<P2<Mealy<A,B>,Stream<B>>,A,P2<Mealy<A,B>,Stream<B>>> leftFold(){
        return new F2<P2<Mealy<A, B>, Stream<B>>, A, P2<Mealy<A, B>, Stream<B>>>() {
            @Override public P2<Mealy<A, B>, Stream<B>> f(P2<Mealy<A, B>, Stream<B>> currentState, A a) {
                Transition<A,B> nextValues = currentState._1().apply( a );
                Stream<B> results = currentState._2().cons( nextValues.result );
                return P.p(nextValues.nextMealy,results);
            }
        };
    }



    public static class Transition<A,B>{
        public final B result;
        public final Mealy<A,B> nextMealy;

        Transition(B result, Mealy<A, B> nextMealy) {
            this.result = result;
            this.nextMealy = nextMealy;
        }
    }

    /**
     * Sideffecting keeper of mealy state.
     * @param <A>
     * @param <B>
     */
    static class UnsafeIncrementingFunction<A,B> extends F<A,B>{

        private volatile Mealy<A,B> mealy;

        public UnsafeIncrementingFunction(final Mealy<A, B> mealy){
            this.mealy = mealy;
        }

        @Override public B f(A a) {
            Transition<A,B> t = mealy.apply( a );
            mealy = t.nextMealy;
            return t.result;
        }
    }

}
