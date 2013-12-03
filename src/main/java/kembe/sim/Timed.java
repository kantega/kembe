package kembe.sim;

import fj.F;
import fj.Ord;
import fj.Show;
import fj.data.Stream;
import kembe.Time;
import kembe.util.Functions;
import kembe.util.Shows;
import org.joda.time.Instant;

public class Timed<T> {

    public final Instant time;
    public final T value;

    public Timed(Instant time, T value) {
        this.time = time;
        this.value = value;
    }

    public static <T> Ord<Timed<T>> timedOrd(){
        return Ord.longOrd.comap( new F<Timed<T>, Long>() {
            @Override public Long f(Timed<T> t) {
                return t.time.getMillis();
            }
        } );
    }

    public static <T> F<T,Timed<T>> timed(final Instant time){
        return new F<T, Timed<T>>() {
            @Override public Timed<T> f(T t) {
                return new Timed(time,t);
            }
        };
    }

    public static <T> F<Timed<T>,Boolean> isBefore(final Instant time){
        return new F<Timed<T>, Boolean>() {
            @Override public Boolean f(Timed<T> tTimed) {
                return tTimed.time.isBefore( time );
            }
        };
    }

    public static <T> F<Timed<T>,Boolean> isEqual(final Instant time){
        return new F<Timed<T>, Boolean>() {
            @Override public Boolean f(Timed<T> tTimed) {
                return tTimed.time.isEqual( time );
            }
        };
    }

    public static <T> F<Timed<T>,Boolean> isBeforeOrEqual(final Instant time){
        return Functions.or(Timed.<T>isBefore( time ),Timed.<T>isEqual( time ));
    }

    public static <T>Show<Timed<T>> show(final Show<T> s){
        return Show.show( new F<Timed<T>, Stream<Character>>() {
            @Override public Stream<Character> f(Timed<T> tTimed) {
                return Stream.fromString( "Timed(at=" )
                        .append( Stream.fromString( tTimed.time.toString() ) )
                        .append( Stream.fromString( ", " ) )
                        .append( s.show( tTimed.value ) );
            }
        } );
    }

    public static <T> Show<Timed<T>> elapsedShow(final Instant start,final Show<T> s){
        return Show.show( new F<Timed<T>, Stream<Character>>() {
            @Override public Stream<Character> f(Timed<T> tTimed) {
                return Stream.fromString( "Timed(elapsed=" )
                        .append( Shows.durationShow.show( Time.from(start).until(tTimed.time).toDuration() ) )
                        .append( Stream.fromString( ", " ) )
                        .append( s.show( tTimed.value ) );
            }
        } );
    }
}
