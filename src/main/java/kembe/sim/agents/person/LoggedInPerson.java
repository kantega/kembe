package kembe.sim.agents.person;

import fj.P2;
import fj.data.List;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import kembe.sim.agents.SignalHandlerContext;

import java.util.Random;

import static fj.P.p;
import static fj.data.List.list;

public class LoggedInPerson extends SignalHandler {

    private final PersonBehaviour behaviour;

    protected LoggedInPerson(PersonBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override public P2<SignalHandler, List<Signal>> signal(Signal signal, Random random, SignalHandlerContext context) {

        if (signal.msg.equals( "tick" )) {

            Boolean willLogout = behaviour.logoutProbability.occurenceWithin( context.intervalSinceLast ).next( random );
            Boolean willDoRandomAction = behaviour.randomactionProbability.occurenceWithin( context.intervalSinceLast ).next( random );

            if (willLogout) {
                return nextState( new IdlePerson( behaviour, false ) );
            }
            else if (willDoRandomAction) {
                ResourceId randomAction = behaviour.actions.next( random );
                ResourceId id = randomAction;
                return p( self(), list( signal.followImmediately( id, "request" ) ) );
            }
        }
        return nextState( self() );
    }


}
