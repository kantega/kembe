package kembe.sim.agents.person;

import kembe.rand.RandomGen;
import kembe.sim.ResourceId;
import kembe.sim.stat.OccurenceProbability;

public class PersonBehaviour {

    public final OccurenceProbability loginProbability;

    public final OccurenceProbability logoutProbability;

    public final OccurenceProbability randomactionProbability;

    public final RandomGen<ResourceId> actions;

    public PersonBehaviour(OccurenceProbability loginProbability, OccurenceProbability logoutProbability, OccurenceProbability randomactionProbability, RandomGen<ResourceId> actions) {
        this.loginProbability = loginProbability;
        this.logoutProbability = logoutProbability;
        this.randomactionProbability = randomactionProbability;
        this.actions = actions;
    }
}
