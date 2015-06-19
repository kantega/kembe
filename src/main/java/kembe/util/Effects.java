package kembe.util;

import fj.F;
import fj.function.Effect1;

public class Effects {

    public static <A> Effect1<A> noOp(){
        return a -> {

        };
    }


    public static <A,B> Effect1<A> comap(Effect1<B> e, F<A,B> f){
        return a -> e.f(f.f(a));
    }

}
