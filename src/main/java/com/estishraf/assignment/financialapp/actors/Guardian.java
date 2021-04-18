package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

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

    private ActorRef<QuoteGenerator.GenerateQuotesCommand> quoteGenerator;

    @Override
    public Receive<AppControlCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootstrapApp.class, this::BootstrapApp)
                .onMessage(TriggerQuoteGeneration.class, this::TriggerQuoteGeneration)
                .build();
    }

    private Behavior<AppControlCommand> BootstrapApp(BootstrapApp command) {
        //#create-actors
        quoteGenerator = getContext().spawn(QuoteGenerator.create(), "QuoteGenerator");
        //#create-actors

        return Behaviors.same();
    }

    private Behavior<AppControlCommand> TriggerQuoteGeneration(TriggerQuoteGeneration command) {
        quoteGenerator.tell(new QuoteGenerator.GenerateNewQuotes());

        return Behaviors.same();
    }
}
