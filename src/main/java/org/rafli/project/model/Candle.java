package org.rafli.project.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Candle {
    private long time;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
}
