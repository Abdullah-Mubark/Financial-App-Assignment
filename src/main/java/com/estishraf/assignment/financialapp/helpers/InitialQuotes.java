package com.estishraf.assignment.financialapp.helpers;

import com.estishraf.assignment.financialapp.models.Quote;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InitialQuotes {
    public static List<Quote> GetQuotes() {
        return new ArrayList<Quote>() {
            {
                add(new Quote("Gold", "Barrick Gold Corporation", 22.23, new Date(), 0.0, 0.0));
                add(new Quote("TSLA", "Tesla, Inc.", 739.78, new Date(), 0.0, 0.0));
                add(new Quote("KO", "The Coca-Cola Company", 53.68, new Date(), 0.0, 0.0));
                add(new Quote("2222.SR", "Saudi Arabian Oil Company", 9.48, new Date(), 0.0, 0.0));
                add(new Quote("7010.SR", "Saudi Telecom Company", 32.75, new Date(), 0.0, 0.0));
            }
        };

    }
}
