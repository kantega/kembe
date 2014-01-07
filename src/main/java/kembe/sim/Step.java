package kembe.sim;

import fj.data.Either;
import fj.data.List;
import kembe.sim.Signal.SignalBuilder;
import kembe.sim.SimEvent.SimEventBuilder;

public class Step {

    public final List<SimEventBuilder> emittedEvents;

    public final Either<SignalSchedule, List<SignalBuilder>> action;

    public final SimAgent nextHandler;

    public Step(List<SimEventBuilder> emittedEvents, Either<SignalSchedule, List<SignalBuilder>> action, SimAgent nextHandler) {
        this.emittedEvents = emittedEvents;
        this.action = action;
        this.nextHandler = nextHandler;
    }

    public static Step sleep(final RandWait sleep, final SignalBuilder sb, final SimAgent nextHandler) {
        return new Step( List.<SimEventBuilder>nil(), Either.<SignalSchedule, List<SignalBuilder>>left( new SignalSchedule( sleep, sb ) ), nextHandler );
    }


    public static Step send(List<SignalBuilder> msgs, SimAgent nextHandler) {
        return new Step( List.<SimEventBuilder>nil(), Either.<SignalSchedule, List<SignalBuilder>>right( msgs ), nextHandler );
    }

    public static Step send(SignalBuilder msg, SimAgent nextHandler) {
        return send( List.single( msg ), nextHandler );
    }

    public Step emit(SimEventBuilder event) {
        return new Step( emittedEvents.cons( event ), action, nextHandler );
    }

    public Step emit(List<SimEventBuilder> events) {
        return new Step( events.append( emittedEvents ), action, nextHandler );
    }

    public Step handledBy(SimAgent nextHandler) {
        return new Step( emittedEvents, action, nextHandler );
    }


}
