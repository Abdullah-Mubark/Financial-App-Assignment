package com.estishraf.assignment.financialapp;


import akka.actor.typed.ActorSystem;
import com.estishraf.assignment.financialapp.actors.Guardian;
import com.estishraf.assignment.financialapp.helpers.Helpers;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;

import java.util.Properties;
import java.util.Set;

public class FinancialApplication {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting App");

        var appProperties = Helpers.GetAppProperties();

        checkKafkaIsUp(appProperties);

        var generationInterval = Integer.parseInt(appProperties.getProperty("generation.interval", "10000"));
        var generationMaxQuotes = Integer.parseInt(appProperties.getProperty("generation.maxquotes", "100"));

        var actorSystem = ActorSystem.create(Guardian.create(), "FinancialApp");
        actorSystem.tell(new Guardian.BootstrapApp());

        for (int i = 0; i < generationMaxQuotes; i++) {
            actorSystem.tell(new Guardian.TriggerQuoteGeneration());
            Thread.sleep(generationInterval);
        }

        System.out.println("Terminating App");
        actorSystem.terminate();
    }

    public static void checkKafkaIsUp(Properties properties) {
        try (AdminClient client = KafkaAdminClient.create(properties)) {
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
        System.out.println("Successfuly connected to Kafka!");
    }

}
