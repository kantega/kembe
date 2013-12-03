package kembe.sim;

import fj.data.Either;
import fj.data.Option;

public class Signal {

    public final String name;

    public final Option<Message> msg;

    public Signal(String name, Option<Message> msg) {
        this.name = name;
        this.msg = msg;
    }

    public static Signal newSignal(String name){
        return new Signal( name,Option.<Message>none() );
    }

    public static Option<Signal> is(String msg, Either<Signal, Message> e) {
        return e.isLeft() && e.left().value().name.equals( msg )
               ? e.left().toOption()
               : Option.<Signal>none();
    }

    public static Option<Signal> contains(String msg, Either<Signal, Message> e) {
        return e.isLeft() && e.left().value().name.contains( msg )
               ? e.left().toOption()
               : Option.<Signal>none();
    }
}
