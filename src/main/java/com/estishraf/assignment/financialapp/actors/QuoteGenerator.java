package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.helpers.Helpers;
import com.estishraf.assignment.financialapp.models.Quote;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    private final KafkaProducer<String, Quote> kafkaProducer;
    private final List<Quote> quotes;
    private final List<ActorRef<Trader.TraderCommand>> traders;

    public QuoteGenerator(ActorContext<GenerateQuotesCommand> context,
                          List<Quote> quotes,
                          KafkaProducer<String, Quote> kafkaProducer,
                          List<ActorRef<Trader.TraderCommand>> traders) {
        super(context);
        this.quotes = quotes;
        this.kafkaProducer = kafkaProducer;
        this.traders = traders;
    }

    public static Behavior<GenerateQuotesCommand> create(List<ActorRef<Trader.TraderCommand>> traders) {
        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, Helpers.GetInitialQuotes(), new KafkaProducer<>(Helpers.GetAppProperties()), traders));
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

        // Generate new quotes
        System.out.println("Generating new quotes: " + new Timestamp(new Date().getTime()));

        List<Quote> newQuotes = new ArrayList<>();
        quotes.forEach(q -> {
            double newPrice;
            double priceChange;
            double pricePercentageChange;

            // 50-50 chance price is going up or down
            if (random.nextBoolean()) {
                var upChangeMultiplier = Helpers.round((0.5) * random.nextDouble(), 2);
                priceChange = Helpers.round(q.LastPrice * upChangeMultiplier, 2);
            } else {
                var downChangeMultiplier = Helpers.round((0.25) * random.nextDouble(), 2);
                priceChange = -(Helpers.round(q.LastPrice * downChangeMultiplier, 2));
            }
            pricePercentageChange = Helpers.round(priceChange / q.LastPrice, 2);
            newPrice = Helpers.round(q.LastPrice + priceChange, 2);

            newQuotes.add(new Quote(q.Symbol, q.Name, newPrice, new Date(), priceChange, pricePercentageChange));
        });

        // Publish new quotes to Kafka
        for (var quote : newQuotes) {
            final ProducerRecord<String, Quote> record =
                    new ProducerRecord<>("quote-events-topic", UUID.randomUUID().toString(), quote);
            kafkaProducer.send(record);
        }

        System.out.println("New quotes published to Kafka: " + new Timestamp(new Date().getTime()));

        // Notify traders about new quotes
        traders.forEach(trader -> trader.tell(new Trader.GetNewQuotes()));

        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, newQuotes, kafkaProducer, traders));
    }
}
