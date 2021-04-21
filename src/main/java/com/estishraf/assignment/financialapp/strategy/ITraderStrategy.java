package com.estishraf.assignment.financialapp.strategy;

import com.estishraf.assignment.financialapp.models.Order;
import com.estishraf.assignment.financialapp.models.Quote;
import com.estishraf.assignment.financialapp.models.StockOwned;

import java.math.BigDecimal;
import java.util.List;

public interface ITraderStrategy {
    List<Order> ShouldBuy(BigDecimal balance, List<Quote> quotesHistory, List<StockOwned> stocksOwned);

    List<Order> ShouldSell(BigDecimal balance, List<Quote> quotesHistory, List<StockOwned> stocksOwned);
}
