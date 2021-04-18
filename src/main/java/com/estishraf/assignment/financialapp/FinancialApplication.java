package com.estishraf.assignment.financialapp;


import akka.actor.typed.ActorSystem;
import com.estishraf.assignment.financialapp.actors.Guardian;

public class FinancialApplication {

    public static void main(String[] args) throws InterruptedException {
        var actorSystem = ActorSystem.create(Guardian.create(), "FinancialApp");
        actorSystem.tell(new Guardian.BootstrapApp());

        for (int i = 0; i < 10; i++) {
            actorSystem.tell(new Guardian.TriggerQuoteGeneration());
            Thread.sleep(1000);
        }

        actorSystem.terminate();
    }

}
