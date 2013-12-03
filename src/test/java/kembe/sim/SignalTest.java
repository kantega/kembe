package kembe.sim;

import org.junit.Assert;
import org.junit.Test;

public class SignalTest {


    @Test
    public void testPrev() {
        Signal one = Signal.signal( AgentId.idFromString( "2" ), AgentId.idFromString( "1" ), "test" );
        Signal two = one.follow( AgentId.idFromString( "3" ), "test" );
        Signal three = two.follow( AgentId.idFromString( "3" ),"" );

        Assert.assertEquals( AgentId.idFromString( "3" ), two.to );
        Assert.assertEquals( AgentId.idFromString( "2" ), two.from );
        Assert.assertEquals( AgentId.idFromString( "2" ), Signal.reply( three, "" ).to );

    }

}
