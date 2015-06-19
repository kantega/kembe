package kembe;

import fj.*;
import fj.data.List;
import fj.data.Validation;
import fj.function.Effect1;
import fj.test.Arg;
import fj.test.CheckResult;
import fj.test.Property;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;

import static fj.test.CheckResult.summary;
import static java.lang.System.out;

public class CheckResults {

    public static Show<Arg<?>> argReflectionShow() {
        return Show.showS( new F<Arg<?>, String>() {
            @Override
            public String f(Arg<?> arg) {
                if (arg.value() instanceof P2) {
                    String s = "Arg(" + Show.p2Show( reflectionShow(), reflectionShow() ).showS( (P2<Object, Object>) arg.value() ) + ")";
                    return s;
                }

                String s = "Arg(" + reflectionShow().showS( arg.value() ) + ")";
                return s;
            }
        } );
    }

    public static <T> Show<T> reflectionShow() {
        return Show.showS( new F<T, String>() {
            @Override
            public String f(T t) {
                if (t instanceof List)
                    return "List("+Show.listShow( reflectionShow() ).showS((List) t )+")";
                else if(t instanceof P1)
                    return Show.p1Show( reflectionShow() ).showS( (P1)t );
                else if(t instanceof P2)
                    return Show.p2Show( reflectionShow(), reflectionShow() ).showS( (P2)t );
                else
                    return ToStringBuilder.reflectionToString( t, ToStringStyle.SHORT_PREFIX_STYLE );
            }
        } );
    }

    public static Property validate(final Validation<? extends Exception, ?> validation) {
        return Property.exception( new P1<Property>() {
            public Property _1() {
                if (validation.isFail())
                    throw new AssertionError( validation.fail() );
                return Property.prop( true );
            }
        } );
    }

    public static void assertAndPrintResults(final List<P2<String, CheckResult>> results) {
        results.foreachDoEffect( new Effect1<P2<String, CheckResult>>() {
            public void f(final P2<String, CheckResult> result) {
                out.print( " * " + result._1() + ": " );
                summary( argReflectionShow() ).println( result._2() );
            }
        } );
        out.println( "--------------------------------------------------------------------------------" );

        assertResults( results );
    }

    public static void assertResults(final List<P2<String, CheckResult>> results) {
        Show<List<String>> showList = Show.listShow( Show.stringShow );

        List<String> resultSummary = results.filter( new F<P2<String, CheckResult>, Boolean>() {
            public Boolean f(final P2<String, CheckResult> result) {
                result._2().exception().foreachDoEffect( new Effect1<Throwable>() {
                    public void f(Throwable throwable) {
                        throw new AssertionError( summary( argReflectionShow() ).showS( result._2() ) + ": " + throwable.getMessage() );
                    }
                } );

                return result._2().isFalsified();
            }
        } ).map( new F<P2<String, CheckResult>, String>() {
            public String f(final P2<String, CheckResult> result) {
                return result._1() + ": " + summary( argReflectionShow() ).showS( result._2() );

            }
        } );
        Assert.assertTrue( showList.showS( resultSummary ), resultSummary.isEmpty() );
    }

}

