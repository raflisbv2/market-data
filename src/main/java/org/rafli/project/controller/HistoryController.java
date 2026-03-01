package org.rafli.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.rafli.project.model.Candle;
import org.rafli.project.service.CandleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "History", description = "Historical market data API")
public class HistoryController {

    private final CandleService candleService;

    public HistoryController(CandleService candleService) {
        this.candleService = candleService;
    }

    @Operation(summary = "Get historical candles", description = "Retrieve aggregated OHLCV candles for a specific symbol and time range.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"s\": \"ok\", \"t\": [1620000000], \"o\": [50000.0], \"h\": [50100.0], \"l\": [49900.0], \"c\": [50050.0], \"v\": [10]}")))
    @GetMapping("/history")
    public Map<String, Object> getHistory(
            @Parameter(description = "Symbol (e.g., BTC-USD)", required = true, example = "BTC-USD")
            @RequestParam String symbol,
            @Parameter(description = "Time interval (1s, 5s, 1m, 15m, 1h)", required = true, example = "1m")
            @RequestParam String interval,
            @Parameter(description = "Start timestamp (Unix seconds)", required = true, example = "1620000000")
            @RequestParam long from,
            @Parameter(description = "End timestamp (Unix seconds)", required = true, example = "1620000600")
            @RequestParam long to) {

        List<Candle> candles = candleService.getHistory(symbol, interval, from, to);

        return Map.of(
                "s", "ok",
                "t", candles.stream().map(Candle::getTime).collect(Collectors.toList()),
                "o", candles.stream().map(Candle::getOpen).collect(Collectors.toList()),
                "h", candles.stream().map(Candle::getHigh).collect(Collectors.toList()),
                "l", candles.stream().map(Candle::getLow).collect(Collectors.toList()),
                "c", candles.stream().map(Candle::getClose).collect(Collectors.toList()),
                "v", candles.stream().map(Candle::getVolume).collect(Collectors.toList())
        );
    }
}
