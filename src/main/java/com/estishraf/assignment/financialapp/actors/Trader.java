package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.enums.OrderResponse;
import com.estishraf.assignment.financialapp.enums.OrderStatus;
import com.estishraf.assignment.financialapp.model.Order;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.model.StockOwned;
import com.estishraf.assignment.financialapp.strategy.ITraderStrategy;
import com.estishraf.assignment.financialapp.utils.AppUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

public class Trader extends AbstractBehavior<Trader.TraderCommand> {

    interface TraderCommand {
    }

    public static class ConsumeNewQuotes implements TraderCommand {
    }

    public static class ProcessOrderResponse implements TraderCommand {
        public final UUID orderId;
        public final OrderResponse orderResponse;

        public ProcessOrderResponse(UUID orderId, OrderResponse orderResponse) {
            this.orderId = orderId;
            this.orderResponse = orderResponse;
        }
    }

    public static class BuyOrderResponse extends ProcessOrderResponse {
        public BuyOrderResponse(UUID orderId, OrderResponse orderResponse) {
            super(orderId, orderResponse);
        }
    }

    public static class SellOrderResponse extends ProcessOrderResponse {
        public SellOrderResponse(UUID orderId, OrderResponse orderResponse) {
            super(orderId, orderResponse);
        }
    }

    private final String name;
    private final BigDecimal balance;
    private final List<Quote> quotesHistory;
    private final List<StockOwned> stocksOwned;
    private final List<Order> orders;
    private final ITraderStrategy strategy;
    private final KafkaConsumer<String, Quote> kafkaConsumer;
    private final ActorRef<Audit.AuditCommand> audit;

    public Trader(ActorContext<TraderCommand> context,
                  String name,
                  BigDecimal balance,
                  List<Quote> quotesHistory,
                  List<StockOwned> stocksOwned,
                  List<Order> orders,
                  ITraderStrategy strategy,
                  KafkaConsumer<String, Quote> kafkaConsumer,
                  ActorRef<Audit.AuditCommand> audit) {
        super(context);
        this.name = name;
        this.balance = balance;
        this.quotesHistory = quotesHistory;
        this.stocksOwned = stocksOwned;
        this.orders = orders;
        this.strategy = strategy;
        this.kafkaConsumer = kafkaConsumer;
        this.audit = audit;
    }

    public static Behavior<TraderCommand> create(String traderName,
                                                 BigDecimal balance,
                                                 ITraderStrategy strategy,
                                                 ActorRef<Audit.AuditCommand> audit) throws Exception {

        // copy default properties
        var consumerProperties = new Properties();
        AppUtil.GetAppProperties().forEach((k, v) -> consumerProperties.setProperty((String) k, (String) v));

        consumerProperties.setProperty("group.id", String.format("quotes-data-%s", traderName));

        var quoteConsumer =
                new KafkaConsumer<String, Quote>(consumerProperties);

        quoteConsumer.subscribe(Collections.singletonList("quote-events-topic"));

        return Behaviors.setup(
                ctx -> new Trader(ctx, traderName, balance, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), strategy, quoteConsumer, audit));
    }

    @Override
    public Receive<TraderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConsumeNewQuotes.class, command -> ConsumeNewQuotes())
                .onMessage(BuyOrderResponse.class, this::ProcessBuyOrderResponse)
                .onMessage(SellOrderResponse.class, this::ProcessSellOrderResponse)
                .build();
    }

    private Behavior<TraderCommand> ConsumeNewQuotes() {
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

            // Call Audit to process order
            audit.tell(new Audit.ProcessOrder(getContext().getSelf(), order, name));
        });

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, balance, quotesHistory, stocksOwned, orders, strategy, kafkaConsumer, audit));
    }

    private Behavior<TraderCommand> ProcessBuyOrderResponse(ProcessOrderResponse command) {
        var buyOrderToProcess = orders.stream()
                .filter(o -> o.id.equals(command.orderId))
                .findFirst().get();

        // Get order and stock list indices
        var indices = GetTraderOrdersAndStocksIndices(buyOrderToProcess.id);
        var orderIndex = indices.getLeft();
        var stockIndex = indices.getRight();

        BigDecimal newBalance;
        List<Order> ordersUpdated = orders;
        List<StockOwned> stocksOwnedUpdated = stocksOwned;

        // Rejected Buy Order
        if (command.orderResponse == OrderResponse.Rejected) {
            // Update trader orders
            buyOrderToProcess.orderStatus = OrderStatus.Declined;
            ordersUpdated.set(orderIndex, buyOrderToProcess);

            // Persist trader balance
            newBalance = balance.add(BigDecimal.ZERO);
        }
        // Accepted Buy Order
        else {
            // Update trader orders
            buyOrderToProcess.orderStatus = OrderStatus.Accepted;
            ordersUpdated.set(orderIndex, buyOrderToProcess);

            // Deduct trader balance
            var deductedBalance = buyOrderToProcess.stock.LastPrice.multiply(BigDecimal.valueOf(buyOrderToProcess.quantity));
            newBalance = balance.subtract(deductedBalance).setScale(2, RoundingMode.UP);

            // Update trader owned stocks
            var stockOwned = stocksOwned.stream()
                    .filter(so -> so.symbol.equals(buyOrderToProcess.stock.Symbol))
                    .findFirst()
                    .orElse(null);

            if (stockOwned == null) {
                stockOwned = new StockOwned(buyOrderToProcess.stock.Symbol, buyOrderToProcess.quantity, buyOrderToProcess.stock.LastPrice);
                stocksOwnedUpdated.add(stockOwned);
            } else {
                var totalStockCost = stockOwned.averagePrice.multiply(BigDecimal.valueOf(stockOwned.quantity)).setScale(2, RoundingMode.HALF_UP);
                var orderCost = buyOrderToProcess.stock.LastPrice.multiply(BigDecimal.valueOf(buyOrderToProcess.quantity)).setScale(2, RoundingMode.HALF_UP);

                var newTotalQuantity = stockOwned.quantity + buyOrderToProcess.quantity;
                var newAveragePrice = totalStockCost.add(orderCost).divide(BigDecimal.valueOf(newTotalQuantity), 2, RoundingMode.HALF_UP);

                stockOwned.quantity = newTotalQuantity;
                stockOwned.averagePrice = newAveragePrice;
                stocksOwnedUpdated.set(stockIndex, stockOwned);
            }
        }

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, newBalance, quotesHistory, stocksOwnedUpdated, ordersUpdated, strategy, kafkaConsumer, audit));
    }

    private Behavior<TraderCommand> ProcessSellOrderResponse(ProcessOrderResponse command) {
        var sellOrderToProcess = orders.stream()
                .filter(o -> o.id.equals(command.orderId))
                .findFirst().get();

        // Get order and stock list indices
        var indices = GetTraderOrdersAndStocksIndices(sellOrderToProcess.id);
        var orderIndex = indices.getLeft();
        var stockIndex = indices.getRight();

        BigDecimal newBalance;
        List<Order> ordersUpdated = orders;
        List<StockOwned> stocksOwnedUpdated = stocksOwned;

        // Rejected Sell Order
        if (command.orderResponse == OrderResponse.Rejected) {
            // Update trader orders
            sellOrderToProcess.orderStatus = OrderStatus.Declined;
            ordersUpdated.set(orderIndex, sellOrderToProcess);

            // Persist trader balance
            newBalance = balance.add(BigDecimal.ZERO);
        }
        // Accepted Sell Order
        else {
            // Update trader orders
            sellOrderToProcess.orderStatus = OrderStatus.Accepted;
            ordersUpdated.set(orderIndex, sellOrderToProcess);

            // Add to trader balance
            var addedBalance = sellOrderToProcess.stock.LastPrice.multiply(BigDecimal.valueOf(sellOrderToProcess.quantity));
            newBalance = balance.add(addedBalance).setScale(2, RoundingMode.UP);

            // Update trader owned stocks
            var stockOwned = stocksOwned.stream()
                    .filter(so -> so.symbol.equals(sellOrderToProcess.stock.Symbol))
                    .findFirst()
                    .orElse(null);

            assert stockOwned != null;
            stockOwned.quantity = stockOwned.quantity - sellOrderToProcess.quantity;

            stocksOwnedUpdated.set(stockIndex, stockOwned);
        }

        return Behaviors.setup(
                ctx -> new Trader(ctx, name, newBalance, quotesHistory, stocksOwnedUpdated, ordersUpdated, strategy, kafkaConsumer, audit));
    }

    private Pair<Integer, Integer> GetTraderOrdersAndStocksIndices(UUID orderId) {
        var orderToProcess = orders.stream()
                .filter(o -> o.id.equals(orderId))
                .findFirst()
                .get();

        var stockToUpdate = stocksOwned.stream()
                .filter(so -> so.symbol.equals(orderToProcess.stock.Symbol))
                .findFirst()
                .orElse(null);

        Integer orderIndex = orders.indexOf(orderToProcess);
        Integer stockIndex = stockToUpdate != null ? stocksOwned.indexOf(stockToUpdate) : -1;

        return Pair.of(orderIndex, stockIndex);
    }
}
