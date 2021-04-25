package com.estishraf.assignment.financialapp.strategy;

import com.estishraf.assignment.financialapp.enums.OrderStatus;
import com.estishraf.assignment.financialapp.enums.OrderType;
import com.estishraf.assignment.financialapp.model.Order;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.model.StockOwned;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleTraderStrategy implements ITraderStrategy {

    @Override
    public List<Order> GenerateOrders(BigDecimal balance, List<Quote> quotesHistory, List<Quote> newQuotes, List<StockOwned> stocksOwned) {
        List<Order> newOrders = new ArrayList<>();
        newQuotes.forEach(quote -> {
            // Guard to not add two orders for same stock
            if (newOrders.stream().anyMatch(s -> s.stock.Symbol.equals(quote.Symbol))) {
                return;
            }

            if (ShouldBuy(quote)) {
                var quantity = CalculateHowMuchToBuy(balance, quote);
                if (quantity == 0) return;
                newOrders.add(new Order(UUID.randomUUID(), quote, quantity, OrderType.Buy, OrderStatus.Pending));
                return;
            }

            if (ShouldSell(quote, stocksOwned)) {
                var quantityOwned = stocksOwned.stream()
                        .filter(so -> so.symbol.equals(quote.Symbol))
                        .findFirst().get()
                        .quantity;
                if (quantityOwned == 0) return;
                newOrders.add(new Order(UUID.randomUUID(), quote, quantityOwned, OrderType.Sell, OrderStatus.Pending));
            }
        });
        return newOrders;
    }

    private boolean ShouldBuy(Quote quote) {
        boolean priceWentDown10PercentOrMore = quote.ChangeInPercentage.compareTo(new BigDecimal("0.1").negate()) <= 0;
        return priceWentDown10PercentOrMore;
    }

    private boolean ShouldSell(Quote quote, List<StockOwned> stocksOwned) {
        var stockOwned = stocksOwned.stream()
                .filter(so -> so.symbol.equals(quote.Symbol))
                .findFirst().orElse(null);

        // Does not own any of this stock
        if (stockOwned == null || stockOwned.quantity == 0)
            return false;

        var changeOfCurrentPriceToAverageBuyPricePercent =
                quote.LastPrice
                        .subtract(stockOwned.averagePrice)
                        .divide(stockOwned.averagePrice, 2, RoundingMode.UP)
                        .multiply(new BigDecimal("100.0"))
                        .setScale(2, RoundingMode.UP);

        boolean priceWentUp10PercentOrMoreFromAverageBuy = changeOfCurrentPriceToAverageBuyPricePercent.compareTo(new BigDecimal("0.15")) >= 0;
        return priceWentUp10PercentOrMoreFromAverageBuy;
    }

    private int CalculateHowMuchToBuy(BigDecimal balance, Quote quote) {
        var buyingCash = balance.multiply(new BigDecimal("0.1"));
        return buyingCash.divide(quote.LastPrice, 0, RoundingMode.CEILING).intValue();
    }
}
