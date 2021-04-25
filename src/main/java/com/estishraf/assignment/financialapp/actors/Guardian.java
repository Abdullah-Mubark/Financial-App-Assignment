package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.repository.TraderRepository;
import com.estishraf.assignment.financialapp.strategy.ITraderStrategy;
import com.estishraf.assignment.financialapp.utils.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class Guardian extends AbstractBehavior<Guardian.AppControlCommand> {

    public interface AppControlCommand {
    }

    public static class BootstrapApp implements AppControlCommand {
    }

    public static class TriggerQuoteGeneration implements AppControlCommand {
    }

    private Guardian(ActorContext<AppControlCommand> context) throws Exception {
        super(context);
    }

    public static Behavior<AppControlCommand> create() {
        return Behaviors.setup(Guardian::new);
    }

    private final List<com.estishraf.assignment.financialapp.entity.Trader> traders = AppUtil.GetInitialTraders();
    private final TraderRepository traderRepository = new TraderRepository();

    private ActorRef<QuoteGenerator.GenerateQuotesCommand> quoteGeneratorActor;

    @Override
    public Receive<AppControlCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootstrapApp.class, this::BootstrapApp)
                .onMessage(TriggerQuoteGeneration.class, this::TriggerQuoteGeneration)
                .build();
    }

    private Behavior<AppControlCommand> BootstrapApp(BootstrapApp command) {
        // Add Traders to db
        traders.forEach(traderRepository::Add);

        List<ActorRef<Trader.TraderCommand>> tradersActors = new ArrayList<>();
        //#create-actors
        traders.forEach(t -> {
            try {
                ITraderStrategy strategy = AppUtil.traderStrategyMapper.get(t.getStrategy());
                tradersActors.add(getContext().spawn(Trader.create(t.getName(), t.getBalance(), strategy), t.getName()));
            } catch (Exception e) {
                System.out.println("error occurred while creating trader actor .. exception: " + e.getMessage());
            }
        });

        quoteGeneratorActor = getContext().spawn(QuoteGenerator.create(tradersActors), "QuoteGenerator-Actor");
        //#create-actors

        return Behaviors.same();
    }

    private Behavior<AppControlCommand> TriggerQuoteGeneration(TriggerQuoteGeneration command) {
        quoteGeneratorActor.tell(new QuoteGenerator.GenerateNewQuotes());

        return Behaviors.same();
    }
}
