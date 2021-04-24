package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.utils.AppUtil;
import com.estishraf.assignment.financialapp.models.Quote;
import com.estishraf.assignment.financialapp.models.StockOwned;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

public class Trader extends AbstractBehavior<Trader.TraderCommand> {

    interface TraderCommand {
    }

    public static class GetNewQuotes implements TraderCommand {
    }

    private final String name;
    private final BigDecimal balance;
    private final KafkaConsumer<String, Quote> kafkaConsumer;
    private final List<Quote> quotesHistory;
    private final List<StockOwned> stocksOwned;

    public Trader(ActorContext<TraderCommand> context,
                  String name, BigDecimal balance,
                  List<Quote> quotesHistory,
                  List<StockOwned> stocksOwned,
                  KafkaConsumer<String, Quote> kafkaConsumer) {
        super(context);
        this.name = name;
        this.balance = balance;
        this.quotesHistory = quotesHistory;
        this.stocksOwned = stocksOwned;
        this.kafkaConsumer = kafkaConsumer;
    }

    public static Behavior<TraderCommand> create(String traderName, BigDecimal balance) throws Exception {

        // copy default properties
        var consumerProperties = new Properties();
        AppUtil.GetAppProperties().forEach((k, v) -> consumerProperties.setProperty((String) k, (String) v));

        consumerProperties.setProperty("group.id", String.format("quotes-data-%s", traderName));

        var quoteConsumer =
                new KafkaConsumer<String, Quote>(consumerProperties);

        quoteConsumer.subscribe(Collections.singletonList("quote-events-topic"));

        return Behaviors.setup(
                ctx -> new Trader(ctx, traderName, balance, new ArrayList<>(), new ArrayList<>(), quoteConsumer));
    }

    @Override
    public Receive<TraderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetNewQuotes.class, command -> GetNewQuotes())
                .build();
    }

    private Behavior<TraderCommand> GetNewQuotes() {
        ConsumerRecords<String, Quote> newQuotes = kafkaConsumer.poll(Duration.ofSeconds(10));

        System.out.printf("%s received %d quote, total quotes stored %s : %s%n",
                name, newQuotes.count(), quotesHistory.size() + newQuotes.count(), new Timestamp(new Date().getTime()));

        for (ConsumerRecord<String, Quote> quote : newQuotes) {
            quotesHistory.add(quote.value());
        }
        Collections.sort(quotesHistory);

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, balance, quotesHistory, stocksOwned, kafkaConsumer));
    }

}
