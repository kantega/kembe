package kembe.util;

import fj.F;
import fj.Show;
import fj.data.List;
import fj.data.Stream;

public class Shows {

    public static <A> Show<List<A>> delimListShow(final Show<A> sa,final String delimiter) {
        return Show.show( new F<List<A>, Stream<Character>>() {
            @Override public Stream<Character> f(List<A> as) {
                return Stream.join( as.toStream().map( sa.show_() ).intersperse( Stream.fromString( delimiter ) ) );
            }
        });
    }

}
