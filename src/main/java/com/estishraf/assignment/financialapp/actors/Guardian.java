package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

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

    private List<ActorRef<Trader.TraderCommand>> traders = new ArrayList<>();
    private ActorRef<QuoteGenerator.GenerateQuotesCommand> quoteGenerator;

    @Override
    public Receive<AppControlCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootstrapApp.class, this::BootstrapApp)
                .onMessage(TriggerQuoteGeneration.class, this::TriggerQuoteGeneration)
                .build();
    }

    private Behavior<AppControlCommand> BootstrapApp(BootstrapApp command) throws Exception {
        //#create-actors
        traders.add(getContext().spawn(Trader.create("Trader 1"), "Trader-1"));
        quoteGenerator = getContext().spawn(QuoteGenerator.create(traders), "QuoteGenerator");
        //#create-actors

        return Behaviors.same();
    }

    private Behavior<AppControlCommand> TriggerQuoteGeneration(TriggerQuoteGeneration command) {
        quoteGenerator.tell(new QuoteGenerator.GenerateNewQuotes());

        return Behaviors.same();
    }
}
