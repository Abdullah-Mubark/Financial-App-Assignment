package com.estishraf.assignment.financialapp.helpers;

import com.estishraf.assignment.financialapp.FinancialApplication;
import com.estishraf.assignment.financialapp.models.Quote;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Helpers {

    private static Properties properties;

    public static Properties GetAppProperties() throws Exception {
        if (properties != null) return properties;
        try (InputStream input = FinancialApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties = new Properties();

            if (input == null) {
                throw new Exception("Sorry, unable to find config.properties");
            }

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
                add(new Quote("Gold", "Barrick Gold Corporation", 22.23, new Date(), 0.0, 0.0));
                add(new Quote("TSLA", "Tesla, Inc.", 739.78, new Date(), 0.0, 0.0));
                add(new Quote("KO", "The Coca-Cola Company", 53.68, new Date(), 0.0, 0.0));
                add(new Quote("2222.SR", "Saudi Arabian Oil Company", 9.48, new Date(), 0.0, 0.0));
                add(new Quote("7010.SR", "Saudi Telecom Company", 32.75, new Date(), 0.0, 0.0));
            }
        };
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
