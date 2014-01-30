package kembe.sim;

import org.joda.time.DateTime;

public class SimAgentContext {
    public final AgentId id;

    public final DateTime currentTime;

    public SimAgentContext(AgentId id, DateTime currentTime) {
        this.id = id;
        this.currentTime = currentTime;
    }
}
