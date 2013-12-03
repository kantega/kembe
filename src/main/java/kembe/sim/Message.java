package kembe.sim;

import fj.F;
import fj.Ord;
import fj.Show;
import fj.data.*;
import fj.function.Strings;
import kembe.util.Shows;

import java.util.Map;
import java.util.UUID;

import static fj.data.Stream.fromString;

public class Message {


    public static final Show<Message> msgShow = Show.showS( new F<Message, String>() {
        @Override public String f(Message message) {
            return message.msg;
        }
    } );

    public static final Show<Message> flowShow = Show.showS( new F<Message, String>() {
        @Override public String f(Message message) {
            return message.from.id + " -[" + message.msg + "]-> ";
        }
    } );

    public static final Show<Message> detailShow = Show.anyShow();

    public static final F<Message, String> getMsg = new F<Message, String>() {
        @Override public String f(Message message) {
            return message.msg;
        }
    };

    public static Show<Message> chainShow = Show.show( new F<Message, Stream<Character>>() {
        @Override public Stream<Character> f(Message message) {

            Stream<Character> list = Shows.delimListShow( flowShow, "" ).show( message.toList().reverse() );

            return fromString( "Message( " )
                    .append( list )
                    .append( fromString( message.to.id ) )
                    .append( fromString( " )" ) );
        }
    } );

    public final UUID id;

    public final AgentId to;

    public final AgentId from;

    public final String msg;

    public final Option<Message> prev;

    public final TreeMap<String, String> params;

    public Message(UUID id, AgentId to, AgentId from, String msg, Option<Message> prev, TreeMap<String, String> params) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.msg = msg;
        this.prev = prev;
        this.params = params;
    }

    public static Message message(AgentId to, String msg, SimAgentContext ctx) {
        return new Message( UUID.randomUUID(), to, ctx.id, msg, Option.<Message>none(), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static Message messageFollowing(Message previous, AgentId to, String msg) {
        return new Message( UUID.randomUUID(), to, previous.to, msg, Option.some( previous ), TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static Message reply(Message previous, String msg) {
        return messageFollowing( previous, previous.from, msg );
    }

    public static F<Message, Boolean> msgEquals(final String msg) {
        return new F<Message, Boolean>() {
            @Override public Boolean f(Message message) {
                return message.msg.equals( msg );
            }
        };
    }

    public static Option<Message> is(F<Message, Boolean> predicate, Either<Signal, Message> m) {
        return m.isRight() && predicate.f( m.right().value() )
               ? m.right().toOption()
               : Option.<Message>none();
    }

    public static Option<Message> contains(String msg, Either<Signal, Message> m) {
        return is( getMsg.andThen( Strings.contains.f( msg ) ), m );
    }

    public static Option<Message> toMessage(Either<Signal, Message> m) {
        return m.right().toOption();
    }

    public Message follow(AgentId to, String msg) {
        return Message.messageFollowing( this, to, msg );
    }

    public TreeMap<String, String> allParams() {
        return prev.option( params, new F<Message, TreeMap<String, String>>() {
            @Override public TreeMap<String, String> f(Message message) {
                //Ugh,Treemap does not have an addAll
                Map<String, String> mm = Message.this.params.toMutableMap();
                mm.putAll( message.allParams().toMutableMap() );
                return TreeMap.fromMutableMap( Ord.stringOrd, mm );
            }
        } );
    }

    public Message reply(String msg) {
        return reply( this, msg );
    }

    public List<Message> toList() {
        if (prev.isNone())
            return List.list( this );
        else
            return prev.some().toList().cons( this );
    }

    @Override public String toString() {
        return "Message(" +
                ", id=" + id +
                ", to=" + to +
                ", from=" + from +
                ", msg='" + msg + '\'' +
                ", params" + params +
                ", prev=" + prev +
                ')';
    }
}
