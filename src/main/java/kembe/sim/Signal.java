package kembe.sim;

import fj.*;
import fj.data.List;
import fj.data.Stream;
import kembe.Time;
import kembe.util.Shows;
import org.joda.time.Instant;

import java.util.UUID;

import static fj.data.Stream.fromString;

public class Signal {

    public static final Ord<Signal> signalOrd = Ord.p2Ord( Time.instantOrd, Ord.<UUID>comparableOrd() ).comap( new F<Signal, P2<Instant, UUID>>() {
        @Override public P2<Instant, UUID> f(Signal signal) {
            return P.p( signal.at, signal.id );
        }
    } );
    public static final Show<Signal> msgShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.msg;
        }
    } );
    public static final Show<Signal> idShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.from.id;
        }
    } );
    public static final Show<Signal> detailShow = Show.anyShow();
    public static Show<Signal> chainShow = Show.show( new F<Signal, Stream<Character>>() {
        @Override public Stream<Character> f(Signal signal) {

            Stream<Character> list = Shows.delimListShow( idShow, " << " ).show( signal.prev );

            return fromString( "Signal( " )
                    .append( fromString( signal.msg + "; " + signal.to.id + " <- " + signal.from.id ) )
                    .append( fromString( " <- " ) )
                    .append( list )
                    .append( fromString( ")" ) );
        }
    } );
    public final UUID id;
    public final ResourceId to;
    public final ResourceId from;
    public final Instant at;
    public final String msg;
    public final List<Signal> prev;

    private Signal(UUID id, ResourceId to, ResourceId from, Instant at, String msg, List<Signal> prev) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.at = at;
        this.msg = msg;
        this.prev = prev;
    }

    public static Signal newSignal(ResourceId to, ResourceId from, Instant at, String msg) {
        return new Signal( UUID.randomUUID(), to, from, at, msg, List.<Signal>nil() );
    }

    public static Signal newSignalFromPrevious(Signal previous, ResourceId to, Instant at, String msg) {
        return new Signal( UUID.randomUUID(), to, previous.to, at, msg, previous.prev.cons( previous ) );
    }

    public Signal follow(ResourceId to, Instant at, String msg) {
        return Signal.newSignalFromPrevious( this, to, at, msg );
    }

    public Signal followImmediately(ResourceId to, String msg) {
        return follow(to, Time.quantumIncrement( at ), msg );
    }

    @Override public String toString() {
        return "Signal(" +
                "at=" + at +
                ", id=" + id +
                ", to=" + to +
                ", from=" + from +
                ", msg='" + msg + '\'' +
                ", prev=" + prev +
                ')';
    }
}
