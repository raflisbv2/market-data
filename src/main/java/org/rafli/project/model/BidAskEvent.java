package org.rafli.project.model;

public record BidAskEvent(String symbol, double bid, double ask, long timestamp) {}
