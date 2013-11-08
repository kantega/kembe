package no.kantega.kembe.sim;

import fj.F;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import kembe.EventStreamHandler;
import kembe.EventStreamSubscriber;
import kembe.Time;
import kembe.sim.rand.RandomGen;
import kembe.sim.ResourceId;
import kembe.sim.Signal;
import kembe.sim.SimulationBuilder;
import kembe.sim.agents.Agent;
import kembe.sim.agents.person.IdlePerson;
import kembe.sim.agents.person.PersonBehaviour;
import kembe.sim.agents.service.IdleIndependentService;
import kembe.sim.stat.OccurenceProbability;
import kembe.sim.stat.Probability;
import org.joda.time.*;
import org.junit.Test;

import java.util.Random;

public class SystemSimulationTest {


    final PersonBehaviour personBehaviour =
            new PersonBehaviour(
                    OccurenceProbability.inRange( new LocalTime( 7, 0 ), new LocalTime( 8, 0 ), Probability.one ),
                    OccurenceProbability.wholeDay( Probability.one ),
                    OccurenceProbability.wholeDay( Probability.one ),
                    RandomGen.oneOf( "serviceOne", "serviceTwo" ).map( ResourceId.fromString )
            );

    final RandomGen<Duration> duration =
            Time.randomDuration( Duration.millis( 10 ), Duration.millis( 100 ) );

    @Test
    public void runSimulation() {
        final Instant now =
                Time.now();

        final List<Agent> drivers =
                List.unfold( new F<Integer, Option<P2<Agent, Integer>>>() {
                    @Override public Option<P2<Agent, Integer>> f(Integer integer) {
                        if (integer > 0)
                            return Option.none();

                        return Option.some( P.p( new Agent( ResourceId.fromString( "person" + integer ), now, new IdlePerson( personBehaviour, false ) ), integer + 1 ) );
                    }
                }, 0 );

        final List<Agent> services =
                List.list(
                        new Agent( ResourceId.fromString( "loginHandler" ), now, new IdleIndependentService( duration ) ),
                        new Agent( ResourceId.fromString( "serviceOne" ), now, new IdleIndependentService( duration ) ),
                        new Agent( ResourceId.fromString( "serviceTwo" ), now, new IdleIndependentService( duration ) )
                );

        SimulationBuilder.build()
                .addDrivers( drivers )
                .addHandlers( services )
                .instant( now, now.plus( Days.days( 60 ).toStandardDuration() ), Minutes.minutes( 10 ), new Random( 1 ) )
                .filter( new F<Signal, Boolean>() {
                    @Override public Boolean f(Signal signal) {
                        return !signal.msg.equals( "tick" );
                    }
                } )
                .open( EventStreamSubscriber.create( new EventStreamHandler<Signal>() {
                    @Override public void next(Signal signal) {
                        System.out.println( signal.at + ": " + Signal.chainShow.showS( signal ) + " ( " + Time.now().toString() + " )" );

                    }

                    @Override public void error(Exception e) {

                    }

                    @Override public void done() {

                    }
                } ) );
    }
}
