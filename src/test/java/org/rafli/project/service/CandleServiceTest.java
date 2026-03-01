package org.rafli.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rafli.project.model.BidAskEvent;
import org.rafli.project.model.Candle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandleServiceTest {

    private CandleService candleService;

    @BeforeEach
    void setUp() {
        candleService = new CandleService();
    }

    @Test
    void testCandleAggregation_SingleEvent() {
        long timestamp = 1620000000L;
        BidAskEvent event = new BidAskEvent("BTC-USD", 50000.0, 50001.0, timestamp);

        candleService.processEvent(event);

        List<Candle> candles = candleService.getHistory("BTC-USD", "1m", timestamp, timestamp + 60);
        assertEquals(1, candles.size());
        Candle candle = candles.get(0);
        assertEquals(timestamp, candle.getTime());
        assertEquals(50000.0, candle.getOpen(), 0.001);
        assertEquals(50000.0, candle.getHigh(), 0.001);
        assertEquals(50000.0, candle.getLow(), 0.001);
        assertEquals(50000.0, candle.getClose(), 0.001);
        assertEquals(1, candle.getVolume());
    }

    @Test
    void testCandleAggregation_MultipleEvents_SameCandle() {
        long timestamp = 1620000000L;
        // Open: 50000
        candleService.processEvent(new BidAskEvent("BTC-USD", 50000.0, 50001.0, timestamp));
        // High: 50100
        candleService.processEvent(new BidAskEvent("BTC-USD", 50100.0, 50101.0, timestamp + 10));
        // Low: 49900
        candleService.processEvent(new BidAskEvent("BTC-USD", 49900.0, 49901.0, timestamp + 20));
        // Close: 50050
        candleService.processEvent(new BidAskEvent("BTC-USD", 50050.0, 50051.0, timestamp + 50));

        List<Candle> candles = candleService.getHistory("BTC-USD", "1m", timestamp, timestamp + 60);
        assertEquals(1, candles.size());
        Candle candle = candles.get(0);
        
        assertEquals(timestamp, candle.getTime());
        assertEquals(50000.0, candle.getOpen(), 0.001);
        assertEquals(50100.0, candle.getHigh(), 0.001);
        assertEquals(49900.0, candle.getLow(), 0.001);
        assertEquals(50050.0, candle.getClose(), 0.001);
        assertEquals(4, candle.getVolume());
    }

    @Test
    void testCandleAggregation_MultipleCandles() {
        long timestamp1 = 1620000000L; // 00:00
        long timestamp2 = 1620000060L; // 00:01

        candleService.processEvent(new BidAskEvent("BTC-USD", 50000.0, 50001.0, timestamp1));
        candleService.processEvent(new BidAskEvent("BTC-USD", 50100.0, 50101.0, timestamp2));

        List<Candle> candles = candleService.getHistory("BTC-USD", "1m", timestamp1, timestamp2 + 60);
        assertEquals(2, candles.size());

        assertEquals(timestamp1, candles.get(0).getTime());
        assertEquals(50000.0, candles.get(0).getClose(), 0.001);

        assertEquals(timestamp2, candles.get(1).getTime());
        assertEquals(50100.0, candles.get(1).getClose(), 0.001);
    }

    @Test
    void testGetHistory_TimeRange() {
        long start = 1620000000L;
        for (int i = 0; i < 5; i++) {
            candleService.processEvent(new BidAskEvent("BTC-USD", 100 + i, 101 + i, start + (i * 60)));
        }

        // Request range covering only the middle 3 candles
        List<Candle> candles = candleService.getHistory("BTC-USD", "1m", start + 60, start + 180);
        assertEquals(3, candles.size());
        assertEquals(start + 60, candles.get(0).getTime());
        assertEquals(start + 180, candles.get(2).getTime());
    }

    @Test
    void testDifferentIntervals() {
        long timestamp = 1620000000L;
        candleService.processEvent(new BidAskEvent("BTC-USD", 50000.0, 50001.0, timestamp));

        // Check 1m candle
        List<Candle> candles1m = candleService.getHistory("BTC-USD", "1m", timestamp, timestamp + 60);
        assertEquals(1, candles1m.size());
        assertEquals(timestamp, candles1m.get(0).getTime());

        // Check 1h candle (should align to hour start)
        // 1620000000 is 2021-05-03T00:00:00Z, so it aligns perfectly
        List<Candle> candles1h = candleService.getHistory("BTC-USD", "1h", timestamp, timestamp + 3600);
        assertEquals(1, candles1h.size());
        assertEquals(timestamp, candles1h.get(0).getTime());
    }
}
