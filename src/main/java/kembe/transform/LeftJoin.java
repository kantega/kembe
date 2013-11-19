package kembe.transform;

import fj.P;
import fj.P2;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import kembe.State;
public class LeftJoin<A,B> extends State<Either<A,B>, Option<P2<A,List<B>>>> {

    final List<B> bs;

    public LeftJoin( List<B> bs) {
        this.bs = bs;
    }

    @Override public Transition<Either<A,B>, Option<P2<A,List<B>>>> apply(Either<A, B> value) {
        if (value.isLeft()) {
            return State.transition(Option.some( P.p( value.left().value(), bs ) ),new LeftJoin<A,B>(List.<B>nil()));
        }else
            return State.transition(Option.<P2<A, List<B>>>none(),new LeftJoin<A,B>( bs.cons( value.right().value() ) ));
    }


}
