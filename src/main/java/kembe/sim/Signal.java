package kembe.sim;

import fj.F;
import fj.Ord;
import fj.Show;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import fj.data.TreeMap;
import kembe.Time;
import kembe.util.Shows;
import org.joda.time.Instant;

import java.util.Map;
import java.util.UUID;

import static fj.data.Stream.fromString;

public class Signal {

    public static final Ord<Signal> signalOrd = Time.instantOrd.comap( new F<Signal, Instant>() {
        @Override public Instant f(Signal signal) {
            return signal.at;
        }
    } );

    public static final Show<Signal> msgShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.msg;
        }
    } );

    public static final Show<Signal> flowShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.from.id +" -[" +signal.msg +"]-> ";
        }
    } );

    public static final Show<Signal> detailShow = Show.anyShow();

    public static Show<Signal> chainShow = Show.show( new F<Signal, Stream<Character>>() {
        @Override public Stream<Character> f(Signal signal) {

            Stream<Character> list = Shows.delimListShow( flowShow, "" ).show( signal.toList().reverse() );

            return fromString( "Signal( " )
                    .append( list )
                    .append( fromString( signal.to.id))
                    .append( fromString( " )" ) );
        }
    } );


    public final UUID id;

    public final ResourceId to;

    public final ResourceId from;

    public final Instant at;

    public final String msg;

    public final Option<Signal> prev;

    public final TreeMap<String, String> params;

    private Signal(UUID id, ResourceId to, ResourceId from, Instant at, String msg, Option<Signal> prev, TreeMap<String, String> params) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.at = at;
        this.msg = msg;
        this.prev = prev;
        this.params = params;
    }

    public static Signal newSignal(ResourceId to, ResourceId from, Instant at, String msg) {
        return new Signal( UUID.randomUUID(), to, from, at, msg, Option.<Signal>none(), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static Signal newSignalFromPrevious(Signal previous, ResourceId to, Instant at, String msg) {
        return new Signal( UUID.randomUUID(), to, previous.to, at, msg, Option.some( previous ), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public Signal follow(ResourceId to, Instant at, String msg) {
        return Signal.newSignalFromPrevious( this, to, at, msg );
    }

    public Signal followImmediately(ResourceId to, String msg) {
        return follow( to, Time.quantumIncrement( at ), msg );
    }

    public Signal withParam(String key, String value) {
        return new Signal( UUID.randomUUID(), to, from, at, msg, prev, params.set( key, value ) );
    }

    public TreeMap<String, String> allParams() {
        return prev.option( params, new F<Signal, TreeMap<String, String>>() {
            @Override public TreeMap<String, String> f(Signal signal) {
                //Ugh,Treemap does not have an addAll
                Map<String, String> mm = Signal.this.params.toMutableMap();
                mm.putAll( signal.allParams().toMutableMap() );
                return TreeMap.fromMutableMap( Ord.stringOrd, mm );
            }
        } );
    }

    public List<Signal> toList() {
        if (prev.isNone())
            return List.list( this );
        else
            return prev.some().toList().cons( this );
    }

    @Override public String toString() {
        return "Signal(" +
                "at=" + at +
                ", id=" + id +
                ", to=" + to +
                ", from=" + from +
                ", msg='" + msg + '\'' +
                ", params" + params +
                ", prev=" + prev +
                ')';
    }

    public static F<Signal,Boolean> msgEquals(final String msg){
        return new F<Signal, Boolean>() {
            @Override public Boolean f(Signal signal) {
                return signal.msg.equals( msg );
            }
        };
    }
}
