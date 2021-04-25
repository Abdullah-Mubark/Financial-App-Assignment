package com.estishraf.assignment.financialapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
public class Quote implements Comparable<Quote> {
    public String Symbol;
    public String Name;
    public BigDecimal LastPrice;
    public Date MarketTime;
    public BigDecimal Change;
    public BigDecimal ChangeInPercentage;

    @Override
    public int compareTo(Quote o) {
        if (MarketTime.compareTo(o.MarketTime) < 0) {
            return -1;
        } else if (MarketTime.compareTo(o.MarketTime) > 0) {
            return 1;
        }
        return 0;
    }
}
