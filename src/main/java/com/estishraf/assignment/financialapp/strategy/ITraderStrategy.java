package com.estishraf.assignment.financialapp.strategy;

import com.estishraf.assignment.financialapp.model.Order;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.model.StockOwned;

import java.math.BigDecimal;
import java.util.List;

public interface ITraderStrategy {
    List<Order> GenerateOrders(BigDecimal balance, List<Quote> quotesHistory, List<Quote> newQuotes, List<StockOwned> stocksOwned);
}
