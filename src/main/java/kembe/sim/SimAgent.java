package kembe.sim;

import fj.data.Either;
import fj.data.List;
import kembe.sim.rand.Rand;
import kembe.sim.stat.Draw;

public abstract class SimAgent {


    public SimAgent self() {
        return this;
    }

    protected Draw<Step> alt(int weight, Step step) {
        return Draw.alt( weight, step );
    }

    protected Rand<Step> just(Step step) {
        return Draw.only( step );
    }

    protected Step sleep(RandWait sleep, String name, SimEvent... events) {
        return Step.sleep( sleep, name, this ).emit( List.list( events ) );
    }

    protected Step sleep(RandWait sleep, String name, Message msg, SimEvent... events) {
        return Step.sleep( sleep, name, msg, this ).emit( List.list( events ) );
    }

    protected Step send(List<Message> msgs) {
        return Step.send( msgs, this );
    }

    protected Step send(Message msg, SimEvent... events) {
        return send( List.single( msg ) ).emit( List.list( events ) );
    }

    protected SimEvent event(String name, SimAgentContext ctx) {
        return SimEvent.newEvent( name, ctx.id );
    }

    public abstract Rand<Step> signal(Either<Signal, Message> message, SimAgentContext context);

}
