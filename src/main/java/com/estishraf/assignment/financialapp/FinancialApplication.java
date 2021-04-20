package com.estishraf.assignment.financialapp;


import akka.actor.typed.ActorSystem;
import com.estishraf.assignment.financialapp.actors.Guardian;
import com.estishraf.assignment.financialapp.helpers.Helpers;
import com.estishraf.assignment.financialapp.models.Quote;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class FinancialApplication {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting App");

        checkKafkaIsUp();

        var appProperties = Helpers.GetAppProperties();
        var quotesGenerationInterval = Integer.parseInt(appProperties.getProperty("generation.interval", "10000"));
        var maxQuotesToGenerate = Integer.parseInt(appProperties.getProperty("generation.maxquotes", "100"));

        var actorSystem = ActorSystem.create(Guardian.create(), "FinancialApp");
        actorSystem.tell(new Guardian.BootstrapApp());

        for (int i = 0; i < maxQuotesToGenerate; i++) {
            actorSystem.tell(new Guardian.TriggerQuoteGeneration());
            Thread.sleep(quotesGenerationInterval);
        }

        System.out.println("Terminating App");
        actorSystem.terminate();
    }

    public static void checkKafkaIsUp() {
        try (AdminClient client = KafkaAdminClient.create(Helpers.GetAppProperties())) {
            ListTopicsResult topics = client.listTopics();
            Set<String> names = topics.names().get();
            if (names.isEmpty()) {
                System.out.println("Kafka has no topics, existing ...");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Kafka is not available .. error: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Successfully connected to Kafka!");
    }

}
