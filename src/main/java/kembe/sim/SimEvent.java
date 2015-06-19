package kembe.sim;

import fj.F;
import fj.Ord;
import fj.Show;
import fj.data.Stream;
import fj.data.TreeMap;
import kembe.util.Shows;

public class SimEvent {

    public static Show<SimEvent> plainShow = Show.show( new F<SimEvent, Stream<Character>>() {
        @Override public Stream<Character> f(SimEvent simEvent) {
            return Stream.fromString( "SimEvent(" )
                    .append( Stream.fromString( simEvent.event ) )
                    .append( Stream.fromString( ", from=" ) )
                    .append( Stream.fromString( simEvent.source.id ) )
                    .append( Stream.fromString( ", properties=" ) )
                    .append( Shows.treeMapShow( Show.stringShow, Show.anyShow() ).show( simEvent.properties ) )
                    .append( Stream.fromString( ")" ) );

        }
    } );

    public final String event;

    public final AgentId source;

    public final TreeMap<String, Object> properties;

    public SimEvent(String event, AgentId source, TreeMap<String, Object> properties) {
        this.event = event;
        this.source = source;
        this.properties = properties;
    }

    public static SimEvent newEvent(String name, AgentId source) {
        return new SimEvent( name, source, TreeMap.<String, Object>empty( Ord.stringOrd ) );
    }

    public static class SimEventBuilder implements F<SimAgentContext, SimEvent> {

        private final String name;

        private final TreeMap<String, Object> properties;

        public SimEventBuilder(String name, TreeMap<String, Object> properties) {
            this.name = name;
            this.properties = properties;
        }

        public SimEventBuilder with(String key, Object value) {
            return new SimEventBuilder( name, properties.set( key, value ) );
        }

        @Override public SimEvent f(SimAgentContext simAgentContext) {
            return new SimEvent( name, simAgentContext.id, properties );
        }
    }
}
