package org.rafli.project.service;

import org.rafli.project.model.BidAskEvent;
import org.rafli.project.model.Candle;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class CandleService {

    // Storage: Symbol -> Interval -> Timestamp -> Candle
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<Long, Candle>>> storage = new ConcurrentHashMap<>();

    // Temporary storage for current building candles: Symbol -> Interval -> Candle
    private final ConcurrentMap<String, ConcurrentMap<String, Candle>> currentCandles = new ConcurrentHashMap<>();

    private final List<String> supportedIntervals = List.of("1s", "5s", "1m", "15m", "1h");

    public void processEvent(BidAskEvent event) {
        for (String interval : supportedIntervals) {
            processEventForInterval(event, interval);
        }
    }

    private void processEventForInterval(BidAskEvent event, String interval) {
        long intervalSeconds = parseInterval(interval);
        long candleTimestamp = (event.timestamp() / intervalSeconds) * intervalSeconds;

        currentCandles.computeIfAbsent(event.symbol(), k -> new ConcurrentHashMap<>())
                .compute(interval, (k, currentCandle) -> {
                    if (currentCandle == null) {
                        return createNewCandle(candleTimestamp, event.bid());
                    } else if (currentCandle.getTime() != candleTimestamp) {
                        // Finalize previous candle
                        saveCandle(event.symbol(), interval, currentCandle);
                        return createNewCandle(candleTimestamp, event.bid());
                    } else {
                        updateCandle(currentCandle, event.bid());
                        return currentCandle;
                    }
                });
        
        // Update storage with current state for immediate query availability
        Candle currentCandle = currentCandles.get(event.symbol()).get(interval);
        if (currentCandle != null) {
             saveCandle(event.symbol(), interval, currentCandle);
        }
    }

    private Candle createNewCandle(long timestamp, double price) {
        return Candle.builder()
                .time(timestamp)
                .open(price)
                .high(price)
                .low(price)
                .close(price)
                .volume(1)
                .build();
    }

    private void updateCandle(Candle candle, double price) {
        candle.setHigh(Math.max(candle.getHigh(), price));
        candle.setLow(Math.min(candle.getLow(), price));
        candle.setClose(price);
        candle.setVolume(candle.getVolume() + 1);
    }

    private void saveCandle(String symbol, String interval, Candle candle) {
        storage.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(interval, k -> new ConcurrentHashMap<>())
                .put(candle.getTime(), candle);
    }

    public List<Candle> getHistory(String symbol, String interval, long from, long to) {
        ConcurrentMap<String, ConcurrentMap<Long, Candle>> symbolMap = storage.get(symbol);
        if (symbolMap == null) {
            return Collections.emptyList();
        }

        ConcurrentMap<Long, Candle> intervalMap = symbolMap.get(interval);
        if (intervalMap == null) {
            return Collections.emptyList();
        }

        return intervalMap.values().stream()
                .filter(c -> c.getTime() >= from && c.getTime() <= to)
                .sorted(Comparator.comparingLong(Candle::getTime))
                .collect(Collectors.toList());
    }

    private long parseInterval(String interval) {
        if (interval.endsWith("s")) {
            return Long.parseLong(interval.replace("s", ""));
        } else if (interval.endsWith("m")) {
            return Long.parseLong(interval.replace("m", "")) * 60;
        } else if (interval.endsWith("h")) {
            return Long.parseLong(interval.replace("h", "")) * 3600;
        }
        throw new IllegalArgumentException("Unsupported interval: " + interval);
    }
}
