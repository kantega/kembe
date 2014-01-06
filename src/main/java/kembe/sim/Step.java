package kembe.sim;

import fj.data.Either;
import fj.data.List;
import kembe.sim.SimEvent.SimEventBuilder;

public class Step {

    public final List<SimEventBuilder> emittedEvents;

    public final Either<SignalSchedule, List<Signal>> action;

    public final SimAgent nextHandler;

    public Step(List<SimEventBuilder> emittedEvents, Either<SignalSchedule, List<Signal>> action, SimAgent nextHandler) {
        this.emittedEvents = emittedEvents;
        this.action = action;
        this.nextHandler = nextHandler;
    }

    public static Step sleep(final RandWait sleep, final String name, final SimAgent nextHandler) {
        return new Step( List.<SimEventBuilder>nil(), Either.<SignalSchedule, List<Signal>>left( new SignalSchedule( sleep, new Signal.SignalF(){
            @Override public Signal f(SimAgentContext ctx) {
                return  Signal.signal(ctx.id , ctx.id,name );
            }
        } ) ), nextHandler );
    }

    public static Step sleep(RandWait sleep, final String name, final Signal prev, SimAgent nextHandler) {
        return new Step( List.<SimEventBuilder>nil(), Either.<SignalSchedule, List<Signal>>left( new SignalSchedule( sleep, new Signal.SignalF() {
            @Override public Signal f(SimAgentContext ctx) {
                return Signal.signalFollowing( prev, ctx.id, name );
            }
        } ) ), nextHandler );
    }

    public static Step send(List<Signal> msgs, SimAgent nextHandler) {
        return new Step( List.<SimEventBuilder>nil(), Either.<SignalSchedule, List<Signal>>right( msgs ), nextHandler );
    }

    public static Step send(Signal msg, SimAgent nextHandler) {
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
