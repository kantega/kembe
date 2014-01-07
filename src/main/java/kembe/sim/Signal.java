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

    public final TreeMap<String, Object> params;

    public Signal(UUID id, AgentId to, AgentId from, String msg, Option<Signal> prev, TreeMap<String, Object> params) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.msg = msg;
        this.prev = prev;
        this.params = params;
    }

    public static Signal newSignal(AgentId to, AgentId from, String msg) {
        return new Signal( UUID.randomUUID(), to, from, msg, Option.<Signal>none(), TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    public static SignalBuilder signal(AgentId to, String msg) {
        return new SignalBuilder( msg, Option.some( to ), Option.<Signal>none(), TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    public static SignalBuilder signalFollowing(Signal previous, AgentId to, String msg) {
        return new SignalBuilder( msg, Option.some( to ), Option.some( previous ), TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    public static SignalBuilder reply(Signal previous, String msg) {
        AgentId replyToId = sender( previous.to, previous );
        return signalFollowing( previous, replyToId, msg );
    }

    public static SignalBuilder newToSelf(String msg) {
        return new SignalBuilder( msg, Option.<AgentId>none(), Option.<Signal>none(), TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    public static SignalBuilder toSelf(String msg,Signal prev) {
        return new SignalBuilder( msg, Option.<AgentId>none(), Option.some(prev), TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    private static AgentId sender(AgentId receiver, Signal signal) {
        if (!signal.from.equals( receiver ))
            return signal.from;
        else if (signal.prev.isSome())
            return sender( receiver, signal.prev.some() );
        else
            return receiver;


    }

    public SignalBuilder follow(AgentId to, String msg) {
        return Signal.signalFollowing( this, to, msg );
    }

    public TreeMap<String, Object> allParams() {
        return prev.option( params, new F<Signal, TreeMap<String, Object>>() {
            @Override public TreeMap<String, Object> f(Signal signal) {
                //Ugh,Treemap does not have an addAll
                Map<String, Object> mm = Signal.this.params.toMutableMap();
                mm.putAll( signal.allParams().toMutableMap() );
                return TreeMap.fromMutableMap( Ord.stringOrd, mm );
            }
        } );
    }

    public SignalBuilder reply(String msg) {
        return reply( this, msg );
    }

    public SignalBuilder toSelf(String msg){
        return Signal.toSelf( msg,this );
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

    public static class SignalBuilder extends F<SimAgentContext, Signal> {

        private final String name;

        private final Option<AgentId> to;

        private final Option<Signal> prev;

        private final TreeMap<String, Object> properties;

        public SignalBuilder(String name, Option<AgentId> to, Option<Signal> prev, TreeMap<String, Object> properties) {
            this.name = name;
            this.to = to;
            this.prev = prev;
            this.properties = properties;
        }

        public SignalBuilder with(String key, Object value) {
            return new SignalBuilder( name, to, prev, properties.set( key, value ) );
        }

        @Override public Signal f(SimAgentContext simAgentContext) {
            return new Signal( UUID.randomUUID(), to.orSome( simAgentContext.id ), simAgentContext.id, name, prev, properties );
        }


    }
}
