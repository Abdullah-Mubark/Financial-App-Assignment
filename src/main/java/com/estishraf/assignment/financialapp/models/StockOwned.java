package com.estishraf.assignment.financialapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StockOwned {
    public String symbol;
    public List<Order> ordersHistory;
}
