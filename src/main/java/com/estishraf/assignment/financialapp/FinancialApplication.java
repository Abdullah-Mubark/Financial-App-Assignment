package com.estishraf.assignment.financialapp;


import akka.actor.typed.ActorSystem;
import com.estishraf.assignment.financialapp.actors.Guardian;
import com.estishraf.assignment.financialapp.helpers.Helpers;

public class FinancialApplication {

    public static void main(String[] args) throws Exception {

        var appProperties = Helpers.GetAppProperties();
        var generationInterval = Integer.parseInt(appProperties.getProperty("generation.interval", "10000"));
        var generationMaxQuotes = Integer.parseInt(appProperties.getProperty("generation.maxquotes", "100"));

        var actorSystem = ActorSystem.create(Guardian.create(), "FinancialApp");
        actorSystem.tell(new Guardian.BootstrapApp());

        for (int i = 0; i < generationMaxQuotes; i++) {
            actorSystem.tell(new Guardian.TriggerQuoteGeneration());
            Thread.sleep(generationInterval);
        }

        actorSystem.terminate();
    }

}
