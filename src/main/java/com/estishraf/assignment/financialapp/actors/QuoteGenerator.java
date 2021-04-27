package com.estishraf.assignment.financialapp.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.estishraf.assignment.financialapp.enums.OrderType;
import com.estishraf.assignment.financialapp.model.Quote;
import com.estishraf.assignment.financialapp.repository.TraderRepository;
import com.estishraf.assignment.financialapp.repository.TraderTransactionRepository;
import com.estishraf.assignment.financialapp.utils.AppUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class QuoteGenerator extends AbstractBehavior<QuoteGenerator.GenerateQuotesCommand> {

    interface GenerateQuotesCommand {
    }

    public static class GenerateNewQuotes implements GenerateQuotesCommand {
    }

    private final Random random = new Random();
    private final KafkaProducer<String, Quote> kafkaProducer;
    private final List<Quote> quotes;
    private final List<ActorRef<Trader.TraderCommand>> traders;

    private final TraderRepository traderRepository = new TraderRepository();
    private final TraderTransactionRepository traderTransactionRepository = new TraderTransactionRepository();
    private final NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);

    public QuoteGenerator(ActorContext<GenerateQuotesCommand> context,
                          List<Quote> quotes,
                          KafkaProducer<String, Quote> kafkaProducer,
                          List<ActorRef<Trader.TraderCommand>> traders) throws Exception {
        super(context);
        this.quotes = quotes;
        this.kafkaProducer = kafkaProducer;
        this.traders = traders;
    }

    public static Behavior<GenerateQuotesCommand> create(List<ActorRef<Trader.TraderCommand>> traders) {
        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, AppUtil.GetInitialQuotes(), new KafkaProducer<>(AppUtil.GetAppProperties()), traders));
    }

    @Override
    public Receive<GenerateQuotesCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateNewQuotes.class, this::GenerateQuote)
                .build();
    }

    private Behavior<GenerateQuotesCommand> GenerateQuote(GenerateNewQuotes command) {

        // Generate new quotes
        List<Quote> newQuotes = new ArrayList<>();
        quotes.forEach(q -> {
            BigDecimal newPrice;
            BigDecimal priceChange;
            BigDecimal pricePercentageChange;

            // 50-50 chance price is going up or down
            if (random.nextBoolean()) {
                var upChangeMultiplier = BigDecimal.valueOf((0.25) * random.nextDouble()).setScale(2, RoundingMode.HALF_UP);
                priceChange = q.LastPrice.multiply(upChangeMultiplier).setScale(2, RoundingMode.HALF_UP);
            } else {
                var downChangeMultiplier = BigDecimal.valueOf((0.20) * random.nextDouble()).setScale(2, RoundingMode.HALF_UP);
                priceChange = (q.LastPrice.multiply(downChangeMultiplier).setScale(2, RoundingMode.HALF_UP)).negate();
            }
            pricePercentageChange = priceChange.divide(q.LastPrice, 2, RoundingMode.HALF_UP);
            newPrice = q.LastPrice.add(priceChange).setScale(2, RoundingMode.HALF_UP);

            newQuotes.add(new Quote(q.Symbol, q.Name, newPrice, new Date(), priceChange, pricePercentageChange));
        });

        // Publish new quotes to Kafka
        for (var quote : newQuotes) {
            final ProducerRecord<String, Quote> record =
                    new ProducerRecord<>("quote-events-topic", UUID.randomUUID().toString(), quote);
            kafkaProducer.send(record);
        }

        System.out.println("New quotes published to Kafka: " + new Timestamp(new Date().getTime()));

        // Notify traders about new quotes
        traders.forEach(trader -> trader.tell(new Trader.ConsumeNewQuotes()));

        // Print current worth of each trader
        PrintTradersWorth(newQuotes);

        return Behaviors.setup(
                ctx -> new QuoteGenerator(ctx, newQuotes, kafkaProducer, traders));
    }

    private void PrintTradersWorth(List<Quote> newQuotes) {
        numberFormatter.setMaximumFractionDigits(3);
        numberFormatter.setMinimumIntegerDigits(3);

        var traders = AppUtil.GetInitialTraders().stream().map(t -> t.getName()).collect(Collectors.toList());
        var tradingStock = AppUtil.GetInitialQuotes().stream().map(q -> q.Symbol).collect(Collectors.toList());

        traders.forEach(traderName -> {
            var trader = traderRepository.Get(traderName);
            var traderTransactions = traderTransactionRepository.GetTraderTransactions(trader);
            var totalStocksWorth = new BigDecimal(0);

            for (var stock : tradingStock) {
                var stockLatestQuote = newQuotes
                        .stream().filter(q -> q.Symbol.equals(stock))
                        .findAny()
                        .get();

                var stockTransactions = traderTransactions
                        .stream()
                        .filter(tt -> tt.getStock().equals(stock))
                        .collect(Collectors.toList());

                var quantityOwned = stockTransactions.stream()
                        // if it is a buy transaction then quantity positive, if sell then quantity negative
                        .mapToInt(tt -> tt.getType().equals(OrderType.Buy) ? tt.getQuantity() : -tt.getQuantity())
                        .sum();

                var stockWorth = stockLatestQuote.LastPrice.multiply(BigDecimal.valueOf(quantityOwned));

                totalStocksWorth = totalStocksWorth.add(stockWorth).setScale(2, RoundingMode.HALF_UP);
            }

            var total = trader.getBalance().add(totalStocksWorth.setScale(2, RoundingMode.HALF_UP));

            System.out.printf("%s -> balance: %s | stocks worth: %s | total: %s %n",
                    traderName,
                    numberFormatter.format(trader.getBalance()),
                    numberFormatter.format(totalStocksWorth),
                    numberFormatter.format(total)
            );
        });
    }
}
