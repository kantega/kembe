package no.kantega;

import fj.F;
import fj.test.Arbitrary;
import fj.test.Property;
import fj.test.reflect.Check;
import fj.test.reflect.Name;
import no.kantega.kembe.CheckResults;
import org.junit.Test;

import java.util.Random;

public class RandomTest {

    @Name("Same seed will always generate same value for a standard java Random")
    Property prop1 = Property.property( Arbitrary.arbLong, new F<Long, Property>() {
        @Override public Property f(Long aLong) {
            return Property.prop( new Random( 1 ).nextInt() == new Random( 1 ).nextInt() );
        }
    } );

    @Test
    public void seed() {
        CheckResults.assertAndPrintResults(
                Check.check( RandomTest.class ) );

    }

}
