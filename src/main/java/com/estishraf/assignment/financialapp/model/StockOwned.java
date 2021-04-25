package com.estishraf.assignment.financialapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StockOwned {
    public String symbol;
    public int quantity;
    public BigDecimal averagePrice;
}
