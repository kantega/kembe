package kembe.sim.behaviour.person;

import fj.P2;
import fj.data.List;
import kembe.rand.DoubleFromZeroIncToOne;
import kembe.rand.RandomGen;
import kembe.sim.RandomSignalHandlerAdapter;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;

public class IdlePerson extends RandomSignalHandlerAdapter<DoubleFromZeroIncToOne> {
    protected IdlePerson() {
        super( RandomGen.randomDouble() );
    }

    @Override protected P2<? extends SignalHandler, List<Signal>> signalRandom(Signal signal, DoubleFromZeroIncToOne randomValue) {

        //må implementere en form for kumulativ sum som må over en terskel korrigert for antall forsøk pr tid.
        return null;
    }
}
