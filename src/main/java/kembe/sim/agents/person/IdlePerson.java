package kembe.sim.agents.person;

import fj.P;
import fj.P2;
import fj.data.List;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SignalHandler;
import kembe.sim.agents.SignalHandlerContext;

import java.util.Random;

public class IdlePerson extends SignalHandler {

    private final PersonBehaviour behaviour;

    private final boolean awaitingLogin;

    public IdlePerson(PersonBehaviour behaviour, boolean awaitingLogin) {
        this.behaviour = behaviour;
        this.awaitingLogin = awaitingLogin;
    }

    @Override public P2<SignalHandler, List<Signal>> signal(Signal signal, Random random, SignalHandlerContext context) {


        if (!awaitingLogin && signal.msg.equals( "tick" )) {
            Boolean willLogin = behaviour.loginProbability.occurenceWithin( context.intervalSinceLast ).next( random );


            if (willLogin) {
                return P.p( awaitLogin(), List.list( signal.followImmediately( ResourceId.fromString( "loginHandler" ), "request" ) ) );
            }
        }
        else if (signal.msg.equals( "ok" )) {
            return nextState( new LoggedInPerson( behaviour ) );
        }

        return nextState( idle() );
    }

    private IdlePerson idle() {
        return new IdlePerson( behaviour, false );
    }

    private SignalHandler awaitLogin() {
        return new IdlePerson( behaviour, true );
    }

}
