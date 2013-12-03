package kembe;

public abstract class State<A,B> {

    public abstract Transition<A,B> apply(A a);


    public State<A,B>self(){
        return this;
    }

    public static class Transition<A,B>{
        public final B result;
        public final State<A,B> nextState;

        Transition(B result, State<A, B> nextState) {
            this.result = result;
            this.nextState = nextState;
        }
    }

    public static <A,B> Transition<A,B> transition(B value, State<A,B> next){
        return new Transition<>(value,next);
    }
}
