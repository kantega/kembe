package kembe.sim;

import org.joda.time.Instant;

public class SimAgentContext {
    public final AgentId id;

    public final Instant currentTime;

    public SimAgentContext(AgentId id, Instant currentTime) {
        this.id = id;
        this.currentTime = currentTime;
    }
}
