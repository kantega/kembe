package kembe.sim.runner;

import fj.data.Either;
import kembe.sim.AgentId;
import kembe.sim.Message;
import kembe.sim.Signal;

public class AgentInvocation {

    public final AgentId id;
    public final Either<Signal,Message> payload;

    public AgentInvocation(AgentId id, Either<Signal, Message> payload) {
        this.id = id;
        this.payload = payload;
    }

}
