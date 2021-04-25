package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.model.Order;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.model.StockOwned;
import com.estishraf.assignment.financialapp.strategy.ITraderStrategy;
import com.estishraf.assignment.financialapp.utils.AppUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Trader extends AbstractBehavior<Trader.TraderCommand> {

    interface TraderCommand {
    }

    public static class GetNewQuotes implements TraderCommand {
    }

    private final String name;
    private final BigDecimal balance;
    private final List<Quote> quotesHistory;
    private final List<StockOwned> stocksOwned;
    private final List<Order> orders;
    private final ITraderStrategy strategy;
    private final KafkaConsumer<String, Quote> kafkaConsumer;


    public Trader(ActorContext<TraderCommand> context,
                  String name,
                  BigDecimal balance,
                  List<Quote> quotesHistory,
                  List<StockOwned> stocksOwned,
                  List<Order> orders,
                  ITraderStrategy strategy,
                  KafkaConsumer<String, Quote> kafkaConsumer) {
        super(context);
        this.name = name;
        this.balance = balance;
        this.quotesHistory = quotesHistory;
        this.stocksOwned = stocksOwned;
        this.orders = orders;
        this.strategy = strategy;
        this.kafkaConsumer = kafkaConsumer;
    }

    public static Behavior<TraderCommand> create(String traderName, BigDecimal balance, ITraderStrategy strategy) throws Exception {

        // copy default properties
        var consumerProperties = new Properties();
        AppUtil.GetAppProperties().forEach((k, v) -> consumerProperties.setProperty((String) k, (String) v));

        consumerProperties.setProperty("group.id", String.format("quotes-data-%s", traderName));

        var quoteConsumer =
                new KafkaConsumer<String, Quote>(consumerProperties);

        quoteConsumer.subscribe(Collections.singletonList("quote-events-topic"));

        return Behaviors.setup(
                ctx -> new Trader(ctx, traderName, balance, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), strategy, quoteConsumer));
    }

    @Override
    public Receive<TraderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetNewQuotes.class, command -> GetNewQuotes())
                .build();
    }

    private Behavior<TraderCommand> GetNewQuotes() {
        ConsumerRecords<String, Quote> consumedQuotes = kafkaConsumer.poll(Duration.ofSeconds(10));

        List<Quote> newQuotes = new ArrayList<>();
        for (ConsumerRecord<String, Quote> consumedQuote : consumedQuotes) {
            newQuotes.add(consumedQuote.value());
            quotesHistory.add(consumedQuote.value());
        }
        Collections.sort(quotesHistory);

        var generatedOrders = strategy.GenerateOrders(balance, quotesHistory, newQuotes, stocksOwned);
        generatedOrders.forEach(order -> {
            orders.add(order);
            // Call Validator
        });

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, balance, quotesHistory, stocksOwned, orders, strategy, kafkaConsumer));
    }

}
