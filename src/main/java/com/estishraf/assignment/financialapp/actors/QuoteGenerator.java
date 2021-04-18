package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import scala.concurrent.duration.FiniteDuration;

import java.sql.Timestamp;
import java.util.Date;

public class QuoteGenerator extends AbstractBehavior<QuoteGenerator.GenerateQuotesCommand> {

    interface GenerateQuotesCommand {
    }

    public static class StartScheduler implements GenerateQuotesCommand {
        final FiniteDuration SCHEDULED_WORK_DELAY;

        public StartScheduler(FiniteDuration scheduled_work_delay) {
            SCHEDULED_WORK_DELAY = scheduled_work_delay;
        }
    }

    public static class GenerateNewQuotes implements GenerateQuotesCommand {
    }

    public QuoteGenerator(ActorContext<GenerateQuotesCommand> context) {
        super(context);
    }

    public static Behavior<GenerateQuotesCommand> create() {
        return Behaviors.setup(QuoteGenerator::new);
    }

    @Override
    public Receive<GenerateQuotesCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateNewQuotes.class, this::GenerateQuote)
                .build();
    }

/*    private Behavior<GenerateQuotesCommand> StartScheduler(StartScheduler startScheduler) {
        System.out.println("Creating scheduler");
        context().system()
                .scheduler()
                .scheduleAtFixedRate(
                        Duration.Zero(), startScheduler.SCHEDULED_WORK_DELAY, context().self(), GenerateNewQuotes.class, context().system().dispatcher(), context().self());
        System.out.println("Finished creating scheduler");
        return this;
    }*/

    private Behavior<GenerateQuotesCommand> GenerateQuote(GenerateNewQuotes command) {
        System.out.println("Generate New Quote: " + new Timestamp(new Date().getTime()));

        return this;
    }
}
