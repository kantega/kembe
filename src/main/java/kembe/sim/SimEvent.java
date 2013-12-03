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
                    .append( Shows.treeMapShow( Show.stringShow, Show.stringShow ).show( simEvent.properties ) )
                    .append( Stream.fromString( ")" ) );

        }
    } );

    public final String event;

    public final AgentId source;

    public final TreeMap<String, String> properties;

    public SimEvent(String event, AgentId source, TreeMap<String, String> properties) {
        this.event = event;
        this.source = source;
        this.properties = properties;
    }

    public static SimEvent newEvent(String name, AgentId source) {
        return new SimEvent( name, source, TreeMap.<String, String>empty( Ord.stringOrd ) );
    }

    public static abstract class SimEventF extends F<SimAgentContext,SimEvent>{}
}
