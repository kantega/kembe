package kembe.util;

import fj.F;
import fj.P2;
import fj.Show;
import fj.data.List;
import fj.data.Stream;
import fj.data.TreeMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Duration;

public class Shows {

    public static <A> Show<List<A>> delimListShow(final Show<A> sa,final String delimiter) {
        return Show.show( new F<List<A>, Stream<Character>>() {
            @Override public Stream<Character> f(List<A> as) {
                return Stream.join( as.toStream().map( sa.show_() ).intersperse( Stream.fromString( delimiter ) ) );
            }
        });
    }

    public static final Show<Duration> durationShow = Show.showS( new F<Duration, String>() {
        @Override public String f(Duration duration) {
            return duration.toString();
        }
    } );

    public static final Show<Exception> exceptionShow = Show.showS( new F<Exception, String>() {
        @Override public String f(Exception e) {
            return e.getMessage();
        }
    } );

    public static <T> Show<T> reflectionShow(){
        return Show.showS( new F<T, String>() {
            @Override public String f(T t) {
                return ToStringBuilder.reflectionToString(t);
            }
        } );
    }

    public static <K,V> Show<TreeMap<K,V>> treeMapShow(final Show<K> kShow, final Show<V> vShow){
        return Show.show(new F<TreeMap<K, V>, Stream<Character>>() {
            @Override public Stream<Character> f(TreeMap<K, V> p2s) {
                return Stream.fromString("TreeMap(")
                        .append( List.iterableList( p2s ).toStream().bind(new F<P2<K, V>, Stream<Character>>() {
                            @Override public Stream<Character> f(P2<K, V> kvp2) {
                                return kShow.show( kvp2._1() )
                                        .append( Stream.fromString( "->" ) )
                                        .append(vShow.show( kvp2._2() ))
                                        .append(Stream.fromString( " " ));
                            }
                        }) )
                        .append(Stream.fromString( ")" ));
            }
        } );
    }


}
