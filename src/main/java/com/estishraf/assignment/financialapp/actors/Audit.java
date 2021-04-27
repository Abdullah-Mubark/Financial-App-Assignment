package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.entity.TraderTransaction;
import com.estishraf.assignment.financialapp.enums.OrderResponse;
import com.estishraf.assignment.financialapp.enums.OrderType;
import com.estishraf.assignment.financialapp.model.Order;
import com.estishraf.assignment.financialapp.repository.TraderRepository;
import com.estishraf.assignment.financialapp.repository.TraderTransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.stream.Collectors;

public class Audit extends AbstractBehavior<Audit.AuditCommand> {

    interface AuditCommand {
    }

    public static class ProcessOrder implements AuditCommand {
        public final ActorRef<Trader.TraderCommand> replyTo;
        public final Order order;
        public final String trader;

        public ProcessOrder(ActorRef<Trader.TraderCommand> replyTo, Order order, String trader) {
            this.replyTo = replyTo;
            this.order = order;
            this.trader = trader;
        }
    }

    private final TraderRepository traderRepository = new TraderRepository();
    private final TraderTransactionRepository traderTransactionRepository = new TraderTransactionRepository();

    public Audit(ActorContext<AuditCommand> context) throws Exception {
        super(context);
    }

    public static Behavior<Audit.AuditCommand> create() {
        return Behaviors.setup(Audit::new);
    }

    @Override
    public Receive<AuditCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProcessOrder.class, this::ProcessOrder)
                .build();
    }

    private Behavior<AuditCommand> ProcessOrder(ProcessOrder orderToProcess) {
        var orderType = orderToProcess.order.orderType;
        var orderId = orderToProcess.order.id;

        // Process Buy order
        if (orderType == OrderType.Buy) {
            var isBuyOrderAccepted = ProcessBuyOrder(orderToProcess);
            if (isBuyOrderAccepted)
                orderToProcess.replyTo.tell(new Trader.BuyOrderResponse(orderId, OrderResponse.Accepted));
            else
                orderToProcess.replyTo.tell(new Trader.BuyOrderResponse(orderId, OrderResponse.Rejected));
        }
        // Process Sell order
        else {
            var isSellOrderAccepted = ProcessSellOrder(orderToProcess);
            if (isSellOrderAccepted)
                orderToProcess.replyTo.tell(new Trader.SellOrderResponse(orderId, OrderResponse.Accepted));
            else
                orderToProcess.replyTo.tell(new Trader.SellOrderResponse(orderId, OrderResponse.Rejected));
        }

        return Behaviors.same();
    }

    private boolean ProcessBuyOrder(ProcessOrder orderToProcess) {
        assert orderToProcess.order.orderType == OrderType.Buy;

        var trader = traderRepository.Get(orderToProcess.trader);
        var quote = orderToProcess.order.stock;
        var quantity = orderToProcess.order.quantity;

        var orderCost = quote.LastPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        var newBalance = trader.getBalance().subtract(orderCost).setScale(2, RoundingMode.HALF_UP);

        // check if no enough balance
        if (newBalance.compareTo(BigDecimal.valueOf(0)) < 0) {
            return false;
        }

        var transaction = new TraderTransaction(UUID.randomUUID(),
                trader,
                quote.Symbol,
                quote.LastPrice,
                quantity,
                OrderType.Buy,
                quote.MarketTime);

        trader.setBalance(newBalance);

        traderTransactionRepository.Add(transaction);
        traderRepository.Update(trader);
        return true;
    }

    private boolean ProcessSellOrder(ProcessOrder orderToProcess) {
        assert orderToProcess.order.orderType == OrderType.Sell;

        var trader = traderRepository.Get(orderToProcess.trader);
        var quote = orderToProcess.order.stock;
        var quantity = orderToProcess.order.quantity;

        var orderProfit = quote.LastPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        var newBalance = trader.getBalance().add(orderProfit).setScale(2, RoundingMode.HALF_UP);

        // filter transactions for this stock
        var traderTransactions = traderTransactionRepository
                .GetByTrader(trader)
                .stream().filter(tt -> tt.getStock().equals(quote.Symbol))
                .collect(Collectors.toList());

        var quantityOwned = traderTransactions.stream()
                // if it is a buy transaction then quantity positive, if sell then quantity negative
                .mapToInt(tt -> tt.getType().equals(OrderType.Buy) ? tt.getQuantity() : -tt.getQuantity())
                .sum();

        // check if no enough stocks owned to sell
        if ((quantityOwned - quantity) < 0) {
            return false;
        }

        var transaction = new TraderTransaction(UUID.randomUUID(),
                trader,
                quote.Symbol,
                quote.LastPrice,
                quantity,
                OrderType.Sell,
                quote.MarketTime);

        trader.setBalance(newBalance);

        traderTransactionRepository.Add(transaction);
        traderRepository.Update(trader);

        return true;
    }
}
