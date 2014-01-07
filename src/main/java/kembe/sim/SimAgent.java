package kembe.sim;

import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
import kembe.sim.Signal.SignalBuilder;
import kembe.sim.SimEvent.SimEventBuilder;
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

    protected Step sleep(RandWait sleep, SignalBuilder signal, SimEventBuilder... events) {
        return Step.sleep( sleep, signal, this ).emit( List.list( events ) );
    }

    protected Step send(List<SignalBuilder> msgs) {
        return Step.send( msgs, this );
    }

    protected Step send(SignalBuilder msg, SimEventBuilder... events) {
        return send( List.single( msg ) ).emit( List.list( events ) );
    }

    protected SimEventBuilder event(final String name) {
        return new SimEventBuilder( name, TreeMap.empty( Ord.stringOrd) );
    }


    public abstract Rand<Step> act(Signal message, SimAgentContext context);

}
