package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Guardian extends AbstractBehavior<Guardian.AppControlCommand> {

    public interface AppControlCommand {
    }

    public static class BootstrapApp implements AppControlCommand {
    }

    public static class TriggerQuoteGeneration implements AppControlCommand {
    }

    private Guardian(ActorContext<AppControlCommand> context) {
        super(context);
    }

    public static Behavior<AppControlCommand> create() {
        return Behaviors.setup(Guardian::new);
    }

    private final List<ActorRef<Trader.TraderCommand>> tradersActors = new ArrayList<>();
    private ActorRef<QuoteGenerator.GenerateQuotesCommand> quoteGeneratorActor;

    @Override
    public Receive<AppControlCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootstrapApp.class, this::BootstrapApp)
                .onMessage(TriggerQuoteGeneration.class, this::TriggerQuoteGeneration)
                .build();
    }

    private Behavior<AppControlCommand> BootstrapApp(BootstrapApp command) throws Exception {
        //#create-actors
        tradersActors.add(getContext().spawn(Trader.create("Trader1", new BigDecimal("10000")), "Trader1-Actor"));
        tradersActors.add(getContext().spawn(Trader.create("Trader2", new BigDecimal("10000")), "Trader2-Actor"));
        tradersActors.add(getContext().spawn(Trader.create("Trader3", new BigDecimal("10000")), "Trader3-Actor"));

        quoteGeneratorActor = getContext().spawn(QuoteGenerator.create(tradersActors), "QuoteGenerator-Actor");
        //#create-actors

        return Behaviors.same();
    }

    private Behavior<AppControlCommand> TriggerQuoteGeneration(TriggerQuoteGeneration command) {
        quoteGeneratorActor.tell(new QuoteGenerator.GenerateNewQuotes());

        return Behaviors.same();
    }
}
