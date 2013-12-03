package kembe.sim;

import fj.F;

public class AgentId {

    public final String id;

    public AgentId(String id) {
        this.id = id;
    }

    public static AgentId idFromString(String id){
        return new AgentId( id );
    }

    public static F<String,AgentId> fromString = new F<String, AgentId>() {
        @Override public AgentId f(String s) {
            return AgentId.idFromString( s );
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentId that = (AgentId) o;

        if (!id.equals( that.id )) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return "AgentId( " + id +" )";
    }
}
