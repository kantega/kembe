package kembe.sim;

import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import kembe.sim.rand.Rand;
import org.joda.time.Instant;

public class Step {

    public final List<SimEvent> emittedEvents;

    public final Either<Occurring<Signal>, List<Message>> action;

    public final SimAgent nextHandler;

    public Step(List<SimEvent> emittedEvents, Either<Occurring<Signal>, List<Message>> action, SimAgent nextHandler) {
        this.emittedEvents = emittedEvents;
        this.action = action;
        this.nextHandler = nextHandler;
    }

    public static Step sleep(Rand<Instant> occurence, String name, SimAgent nextHandler) {
        return new Step( List.<SimEvent>nil(), Either.<Occurring<Signal>, List<Message>>left( new Occurring<>( occurence, new Signal(name, Option.<Message>none()) ) ), nextHandler );
    }

    public static Step sleep(Rand<Instant> occurence, String name, Message msg, SimAgent nextHandler) {
        return new Step( List.<SimEvent>nil(), Either.<Occurring<Signal>, List<Message>>left( new Occurring<>( occurence, new Signal(name, Option.<Message>some(msg)) ) ), nextHandler );
    }

    public static Step send(List<Message> msgs, SimAgent nextHandler) {
        return new Step( List.<SimEvent>nil(), Either.<Occurring<Signal>, List<Message>>right( msgs ), nextHandler );
    }

    public static Step send(Message msg, SimAgent nextHandler) {
        return send( List.single( msg ), nextHandler );
    }

    public Step emit(SimEvent event) {
        return new Step( emittedEvents.cons( event ), action, nextHandler );
    }

    public Step emit(List<SimEvent> events) {
        return new Step( events.append( emittedEvents ), action, nextHandler );
    }

    public Step handledBy(SimAgent nextHandler) {
        return new Step( emittedEvents, action, nextHandler );
    }
}
