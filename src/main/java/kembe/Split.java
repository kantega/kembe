package kembe;

import fj.F;
import fj.P;
import fj.P2;

public class Split<A, B> {

    private final EventStream<A> a;

    private final EventStream<B> b;

    public Split(EventStream<A> a, EventStream<B> b) {
        this.a = a;
        this.b = b;
    }

    public EventStream<A> _1() {
        return a;
    }


    public EventStream<B> _2(){
        return b;
    }

    public P2<EventStream<A>,EventStream<B>> toP2(){
        return P.p( a,b );
    }

    public <C> Split<C,B> map1(F<A,C> f){
        return new Split(a.map(f),b);
    }

    public <D> Split<A,D> map2(F<B,D> f){
        return new Split(a,b.map(f));
    }

}
