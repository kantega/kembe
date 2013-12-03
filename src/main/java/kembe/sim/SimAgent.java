package kembe.sim;

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

    protected Step sleep(RandWait sleep, String name, SimEvent.SimEventF... events) {
        return Step.sleep( sleep, name, this ).emit( List.list( events ) );
    }

    protected Step sleep(RandWait sleep, String name, Signal prev, SimEvent.SimEventF... events) {
        return Step.sleep( sleep, name,prev, this ).emit( List.list( events ) );
    }

    protected Step send(List<Signal> msgs) {
        return Step.send( msgs, this );
    }

    protected Step send(Signal msg, SimEvent.SimEventF... events) {
        return send( List.single( msg ) ).emit( List.list( events ) );
    }

    protected SimEvent.SimEventF event(final String name) {
        return new SimEvent.SimEventF() {
            @Override public SimEvent f(SimAgentContext ctx) {
                return SimEvent.newEvent( name, ctx.id );
            }
        };

    }

    public abstract Rand<Step> act(Signal message, SimAgentContext context);

}
