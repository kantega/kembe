package kembe;

import fj.F;
import fj.data.Either;
import fj.data.Option;

public class Functions {

    public static <A,B> F<Either<A,B>,Option<A>> left(){
        return new F<Either<A, B>, Option<A>>() {
            @Override
            public Option<A> f(Either<A, B> abEither) {
                return abEither.left().toOption();
            }
        };
    }

    public static <A,B> F<Either<A,B>,Option<B>> right(){
        return new F<Either<A, B>, Option<B>>() {
            @Override
            public Option<B> f(Either<A, B> abEither) {
                return abEither.right().toOption();

            }
        };
    }

    public static <A> F<Option<A>,Boolean> isSome(){
        return new F<Option<A>, Boolean>() {
            @Override
            public Boolean f(Option<A> as) {
                return as.isSome();
            }
        };
    }

    public static <A> F<Option<A>,A> getSome(){
        return new F<Option<A>, A>() {
            @Override
            public A f(Option<A> as) {
                return as.some();
            }
        };
    }
}
