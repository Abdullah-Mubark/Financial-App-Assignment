package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.helpers.Helpers;
import com.estishraf.assignment.financialapp.models.Quote;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

public class Trader extends AbstractBehavior<Trader.TraderCommand> {

    interface TraderCommand {
    }

    public static class GetNewQuotes implements TraderCommand {
    }

    private final String name;
    private final KafkaConsumer<String, Quote> kafkaConsumer;
    private final List<Quote> quotesHistory;

    public Trader(ActorContext<TraderCommand> context, String name, List<Quote> quotesHistory, KafkaConsumer<String, Quote> kafkaConsumer) {
        super(context);
        this.name = name;
        this.quotesHistory = quotesHistory;
        this.kafkaConsumer = kafkaConsumer;
    }

    public static Behavior<TraderCommand> create(String traderName) throws Exception {

        // copy default properties
        var consumerProperties = new Properties();
        Helpers.GetAppProperties().forEach((k, v) -> {
            consumerProperties.setProperty((String) k, (String) v);
        });

        consumerProperties.setProperty("group.id", String.format("quotes-data-%s", traderName));

        var quoteConsumer =
                new KafkaConsumer<String, Quote>(consumerProperties);

        quoteConsumer.subscribe(Collections.singletonList("quote-events-topic"));

        return Behaviors.setup(
                ctx -> new Trader(ctx, traderName, new ArrayList<>(), quoteConsumer));
    }

    @Override
    public Receive<TraderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetNewQuotes.class, this::GetNewQuotes)
                .build();
    }

    private Behavior<TraderCommand> GetNewQuotes(GetNewQuotes command) {
        ConsumerRecords<String, Quote> newQuotes = kafkaConsumer.poll(Duration.ofSeconds(10));

        System.out.printf("%s received %d quote, total quotes stored %s : %s%n",
                name, newQuotes.count(), quotesHistory.size() + newQuotes.count(), new Timestamp(new Date().getTime()));

        for (ConsumerRecord<String, Quote> quote : newQuotes) {
            quotesHistory.add(quote.value());
        }

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, quotesHistory, kafkaConsumer));
    }

}
