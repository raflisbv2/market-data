package org.rafli.project.kafka;

import org.rafli.project.model.BidAskEvent;
import org.rafli.project.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MarketDataListener {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataListener.class);

    private final CandleService candleService;

    public MarketDataListener(CandleService candleService) {
        this.candleService = candleService;
    }

    @KafkaListener(topics = "market-data", groupId = "market-data-group")
    public void listen(BidAskEvent event) {
        logger.info("Received event: {}", event);
        candleService.processEvent(event);
    }
}
