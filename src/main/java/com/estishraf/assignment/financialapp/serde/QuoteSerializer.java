package com.estishraf.assignment.financialapp.serde;

import com.estishraf.assignment.financialapp.models.Quote;
import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class QuoteSerializer implements Closeable, AutoCloseable, Serializer<Quote> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    static private final Gson gson = new Gson();

    @Override
    public void configure(Map configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Quote data) {
        // Transform the Quote object to String
        String line = gson.toJson(data);
        // Return the bytes from the String 'line'
        return line.getBytes(CHARSET);
    }

    @Override
    public void close() {

    }
}
