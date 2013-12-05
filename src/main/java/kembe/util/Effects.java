package kembe.util;

import fj.Effect;

public class Effects {

    public static <A> Effect<A> noOp(){
        return new Effect<A>() {
            @Override public void e(A a) {

            }
        };
    }

}
