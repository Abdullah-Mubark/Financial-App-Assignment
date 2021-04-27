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

public class GoodTraderStrategy implements ITraderStrategy {

    @Override
    public List<Order> GenerateOrders(BigDecimal balance, List<Quote> quotesHistory, List<Quote> newQuotes, List<StockOwned> stocksOwned) {
        List<Order> newOrders = new ArrayList<>();
        newQuotes.forEach(quote -> {
            // Guard to not add two orders for same stock
            if (newOrders.stream().anyMatch(s -> s.stock.Symbol.equals(quote.Symbol))) {
                return;
            }

            if (ShouldBuy(balance, quotesHistory, quote)) {
                var quantity = CalculateHowMuchToBuy(balance, quotesHistory, quote);
                if (quantity == 0) return;
                newOrders.add(new Order(UUID.randomUUID(), quote, quantity, OrderType.Buy, OrderStatus.Pending));
                return;
            }

            if (ShouldSell(quote, stocksOwned)) {
                var quantityOwned = CalculateHowManyToSell(stocksOwned, quote);
                if (quantityOwned == 0) return;
                newOrders.add(new Order(UUID.randomUUID(), quote, quantityOwned, OrderType.Sell, OrderStatus.Pending));
            }
        });
        return newOrders;
    }

    private boolean ShouldBuy(BigDecimal balance, List<Quote> quotesHistory, Quote quote) {
        if (balance.compareTo(new BigDecimal(1000)) <= 0)
            return false;

        var stockPriceHistoryAverage =
                CalculateStockPriceHistoryAverage(quotesHistory, quote.Symbol);

        var changeOfCurrentPriceToStockPriceHistoryAveragePercent =
                CalculateChangeOfCurrentPriceToStockHistoryAveragePricePercent(quote.LastPrice, stockPriceHistoryAverage);

        return changeOfCurrentPriceToStockPriceHistoryAveragePercent.compareTo(new BigDecimal("0.20").negate()) <= 0;
    }

    private boolean ShouldSell(Quote quote, List<StockOwned> stocksOwned) {
        var stockOwned = stocksOwned.stream()
                .filter(so -> so.symbol.equals(quote.Symbol))
                .findFirst().orElse(null);

        // Does not own any of this stock
        if (stockOwned == null || stockOwned.quantity == 0)
            return false;

        var changeOfCurrentPriceToAverageBuyPricePercent =
                CalculateChangeOfCurrentPriceToAverageBuyPricePercent(quote.LastPrice, stockOwned.averagePrice);

        return changeOfCurrentPriceToAverageBuyPricePercent.compareTo(new BigDecimal("0.30")) >= 0;
    }

    private int CalculateHowMuchToBuy(BigDecimal balance, List<Quote> quotesHistory, Quote quote) {
        var stockPriceHistoryAverage =
                CalculateStockPriceHistoryAverage(quotesHistory, quote.Symbol);

        var changeOfCurrentPriceToStockPriceHistoryAveragePercent =
                CalculateChangeOfCurrentPriceToStockHistoryAveragePricePercent(quote.LastPrice, stockPriceHistoryAverage);

        BigDecimal percentageOfCash;
        if (changeOfCurrentPriceToStockPriceHistoryAveragePercent.compareTo(new BigDecimal("0.50").negate()) <= 0) {
            percentageOfCash = new BigDecimal("0.30");
        } else {
            percentageOfCash = new BigDecimal("0.15");
        }

        var buyingCash = balance.multiply(percentageOfCash);
        return buyingCash.divide(quote.LastPrice, 0, RoundingMode.CEILING).intValue();
    }

    private int CalculateHowManyToSell(List<StockOwned> stocksOwned, Quote quote) {
        var stock = stocksOwned.stream()
                .filter(so -> so.symbol.equals(quote.Symbol))
                .findFirst().get();

        var changeOfCurrentPriceToAverageBuyPricePercent =
                CalculateChangeOfCurrentPriceToAverageBuyPricePercent(quote.LastPrice, stock.averagePrice);

        double percentageOfStocksOwnedToSell;
        if (changeOfCurrentPriceToAverageBuyPricePercent.compareTo(new BigDecimal("0.50")) >= 0) {
            percentageOfStocksOwnedToSell = 0.5;
        } else {
            percentageOfStocksOwnedToSell = 0.25;
        }

        var numberOfStockToBuy = stock.quantity * percentageOfStocksOwnedToSell;
        return (int) numberOfStockToBuy;
    }

    private BigDecimal CalculateStockPriceHistoryAverage(List<Quote> quotesHistory, String stock) {
        BigDecimal[] stockPriceTotalWithCount
                = quotesHistory.stream()
                .filter(s -> s.Symbol.equals(stock))
                .map(q -> new BigDecimal[]{q.LastPrice, BigDecimal.ONE})
                .reduce((a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(BigDecimal.ONE)})
                .get();

        return stockPriceTotalWithCount[0].divide(stockPriceTotalWithCount[1], 2, RoundingMode.HALF_UP);
    }

    private BigDecimal CalculateChangeOfCurrentPriceToAverageBuyPricePercent(BigDecimal currentPrice,
                                                                             BigDecimal averageBuyPrice) {
        return currentPrice
                .subtract(averageBuyPrice)
                .divide(averageBuyPrice, 2, RoundingMode.UP)
                .setScale(2, RoundingMode.UP);
    }

    private BigDecimal CalculateChangeOfCurrentPriceToStockHistoryAveragePricePercent(BigDecimal currentPrice,
                                                                                      BigDecimal stockHistoryAveragePrice) {
        return currentPrice
                .subtract(stockHistoryAveragePrice)
                .divide(stockHistoryAveragePrice, 2, RoundingMode.UP)
                .setScale(2, RoundingMode.UP);
    }
}
