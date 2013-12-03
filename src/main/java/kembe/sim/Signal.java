package kembe.sim;

import fj.F;
import fj.Ord;
import fj.Show;
import fj.data.*;
import kembe.util.Shows;

import java.util.Map;
import java.util.UUID;

import static fj.data.Stream.fromString;

public class Signal {


    public static final Show<Signal> msgShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.msg;
        }
    } );

    public static final Show<Signal> flowShow = Show.showS( new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.from.id + " -[" + signal.msg + "]-> ";
        }
    } );

    public static final Show<Signal> detailShow = Show.anyShow();

    public static final F<Signal, String> getMsg = new F<Signal, String>() {
        @Override public String f(Signal signal) {
            return signal.msg;
        }
    };

    public static Show<Signal> chainShow = Show.show( new F<Signal, Stream<Character>>() {
        @Override public Stream<Character> f(Signal signal) {

            Stream<Character> list = Shows.delimListShow( flowShow, "" ).show( signal.toList().reverse() );

            return fromString( "Signal( " )
                    .append( list )
                    .append( fromString( signal.to.id ) )
                    .append( fromString( " )" ) );
        }
    } );

    public final UUID id;

    public final AgentId to;

    public final AgentId from;

    public final String msg;

    public final Option<Signal> prev;

    public final TreeMap<String, String> params;

    public Signal(UUID id, AgentId to, AgentId from, String msg, Option<Signal> prev, TreeMap<String, String> params) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.msg = msg;
        this.prev = prev;
        this.params = params;
    }

    public static Signal signal(AgentId to, AgentId from, String msg) {
        return new Signal( UUID.randomUUID(), to, from, msg, Option.<Signal>none(), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static Signal signalFollowing(Signal previous, AgentId to, String msg) {
        return new Signal( UUID.randomUUID(), to, previous.to, msg, Option.some( previous ), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static Signal reply(Signal previous, String msg) {
        AgentId replyToId = sender(previous.to,previous);
        return signalFollowing( previous, replyToId, msg );
    }

    public static F<Signal, Boolean> msgEquals(final String msg) {
        return new F<Signal, Boolean>() {
            @Override public Boolean f(Signal signal) {
                return signal.msg.equals( msg );
            }
        };
    }

    public static Option<Signal> toSignal(Either<Signal, Signal> m) {
        return m.right().toOption();
    }

    private static AgentId sender(AgentId receiver, Signal signal) {
        if (!signal.from.equals( receiver ))
            return signal.from;
        else if (signal.prev.isSome())
            return sender( receiver, signal.prev.some() );
        else
            return receiver;


    }

    public Signal follow(AgentId to, String msg) {
        return Signal.signalFollowing( this, to, msg );
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

    public Signal reply(String msg) {
        return reply( this, msg );
    }

    public List<Signal> toList() {
        if (prev.isNone())
            return List.list( this );
        else
            return prev.some().toList().cons( this );
    }

    @Override public String toString() {
        return "Signal(" +
                ", to=" + to +
                ", from=" + from +
                ", msg='" + msg + '\'' +
                ')';
    }

    public static abstract class SignalF extends F<SimAgentContext,Signal>{}
}
