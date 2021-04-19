package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.helpers.InitialQuotes;
import com.estishraf.assignment.financialapp.models.Quote;
import scala.concurrent.duration.FiniteDuration;

import java.sql.Timestamp;
import java.util.*;

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

    private final Random random = new Random();

    List<Quote> quotes;

    public QuoteGenerator(ActorContext<GenerateQuotesCommand> context, List<Quote> quotes) {
        super(context);
        this.quotes = quotes;
    }

    public static Behavior<GenerateQuotesCommand> create() {
        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, InitialQuotes.GetQuotes()));
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

        List<Quote> newQuotes = new ArrayList<>();
        quotes.forEach(q -> {
            double newPrice;
            double priceChange;
            double pricePercentageChange;

            // 50-50 chance price is going up or down
            if (random.nextBoolean()) {
                var upChangeMultiplier = (0.5) * random.nextDouble();
                priceChange = q.LastPrice * upChangeMultiplier;
            } else {
                var downChangeMultiplier = (0.25) * random.nextDouble();
                priceChange = -(q.LastPrice * downChangeMultiplier);
            }
            pricePercentageChange = priceChange / q.LastPrice;
            newPrice = q.LastPrice + priceChange;

            newQuotes.add(new Quote(q.Symbol, q.Name, newPrice, new Date(), priceChange, pricePercentageChange));
        });

        newQuotes.forEach(nq -> System.out.println(nq.toString()));

        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, newQuotes));
    }
}
