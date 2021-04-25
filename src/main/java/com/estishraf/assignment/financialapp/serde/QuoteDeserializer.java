package com.estishraf.assignment.financialapp.serde;

import com.estishraf.assignment.financialapp.model.Quote;
import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class QuoteDeserializer implements Closeable, AutoCloseable, Deserializer<Quote> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    static private final Gson gson = new Gson();

    @Override
    public void configure(Map configs, boolean isKey) {
    }

    @Override
    public Quote deserialize(String topic, byte[] data) {
        try {
            // Transform the bytes to String
            String quote = new String(data, CHARSET);
            // Return the Quote object created from the String 'quote'
            return gson.fromJson(quote, Quote.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading bytes", e);
        }
    }

    @Override
    public void close() {

    }
}
