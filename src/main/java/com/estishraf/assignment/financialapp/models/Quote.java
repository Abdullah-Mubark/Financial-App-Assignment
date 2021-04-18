package com.estishraf.assignment.financialapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Quote {
    public String Symbol;
    public String Name;
    public double LastPrice;
    public Date MarketTime;
    public double Change;
    public double ChangeInPercentage;
}
