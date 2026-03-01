package org.rafli.project.kafka;

import org.rafli.project.model.BidAskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Component
public class MarketDataProducer {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataProducer.class);
    private final KafkaTemplate<String, BidAskEvent> kafkaTemplate;
    private final Random random = new Random();
    private final List<String> symbols = List.of("BTC-USD", "ETH-USD");

    public MarketDataProducer(KafkaTemplate<String, BidAskEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 5000) // Publish every 5s
    public void publishRandomEvent() {
        String symbol = symbols.get(random.nextInt(symbols.size()));
        double basePrice = symbol.equals("BTC-USD") ? 30000.0 : 2000.0;
        double price = basePrice + (random.nextDouble() * 100 - 50);
        double bid = price - random.nextDouble();
        double ask = price + random.nextDouble();
        long timestamp = Instant.now().getEpochSecond();

        BidAskEvent event = new BidAskEvent(symbol, bid, ask, timestamp);
        logger.info("Publishing event: {}", event);
        kafkaTemplate.send("market-data", symbol, event);
    }
}
