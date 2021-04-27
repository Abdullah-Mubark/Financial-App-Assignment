package com.estishraf.assignment.financialapp.utils;

import com.estishraf.assignment.financialapp.FinancialApplication;
import com.estishraf.assignment.financialapp.entity.Trader;
import com.estishraf.assignment.financialapp.enums.TraderStrategy;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.strategy.BadTraderStrategy;
import com.estishraf.assignment.financialapp.strategy.BasicTraderStrategy;
import com.estishraf.assignment.financialapp.strategy.GoodTraderStrategy;
import com.estishraf.assignment.financialapp.strategy.ITraderStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

public class AppUtil {

    private static Properties properties;

    public static final Map<TraderStrategy, ITraderStrategy> traderStrategyMapper = Map.of(
            TraderStrategy.Basic, new BasicTraderStrategy(),
            TraderStrategy.Bad, new BadTraderStrategy(),
            TraderStrategy.Good, new GoodTraderStrategy()
    );

    public static Properties GetAppProperties() throws Exception {
        if (properties != null) return properties;
        try (InputStream input = FinancialApplication.class.getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new Exception("Sorry, unable to find config.properties");
            }

            properties = new Properties();

            //load a properties file from class path, inside static method
            properties.load(input);

            return properties;
        } catch (IOException io) {
            throw new Exception(io.getMessage());
        }
    }

    public static List<Quote> GetInitialQuotes() {
        return new ArrayList<>() {
            {
                add(new Quote("Gold", "Barrick Gold Corporation", new BigDecimal("22.23"), new Date(), new BigDecimal("0"), new BigDecimal("0")));
                add(new Quote("TSLA", "Tesla, Inc.", new BigDecimal("739.78"), new Date(), new BigDecimal("0"), new BigDecimal("0")));
                add(new Quote("KO", "The Coca-Cola Company", new BigDecimal("53.68"), new Date(), new BigDecimal("0"), new BigDecimal("0")));
                add(new Quote("2222.SR", "Saudi Arabian Oil Company", new BigDecimal("9.48"), new Date(), new BigDecimal("0"), new BigDecimal("0")));
                add(new Quote("7010.SR", "Saudi Telecom Company", new BigDecimal("32.75"), new Date(), new BigDecimal("0"), new BigDecimal("0")));
            }
        };
    }

    public static List<Trader> GetInitialTraders() {
        return new ArrayList<>() {
            {
                add(new Trader("Trader-1", new BigDecimal("10000.0"), TraderStrategy.Basic));
                add(new Trader("Trader-2", new BigDecimal("10000.0"), TraderStrategy.Bad));
                add(new Trader("Trader-3", new BigDecimal("10000.0"), TraderStrategy.Good));
            }
        };
    }
}
