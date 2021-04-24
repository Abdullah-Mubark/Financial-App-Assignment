package com.estishraf.assignment.financialapp.utils;

import com.estishraf.assignment.financialapp.FinancialApplication;
import com.estishraf.assignment.financialapp.models.Quote;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class AppUtil {

    private static Properties properties;

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

}
