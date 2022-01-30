package com.azure.servicebus.issues;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

//Local Test
public class TestDurationCalculation {
    public static void main(String[] args) {
        //When expireOn is 1970-1-1
        long epochSeconds = Long.parseLong("0");
        OffsetDateTime expireOn = Instant.ofEpochSecond(epochSeconds).atOffset(ZoneOffset.UTC);

        Duration between = Duration.between(OffsetDateTime.now(ZoneOffset.UTC), expireOn);
        long refreshSeconds = (long)Math.floor((double)between.getSeconds() * 0.9D);
        long refreshIntervalMS = refreshSeconds * 1000;
        Duration firstInterval = Duration.ofMillis(refreshIntervalMS);

        Flux.interval(firstInterval);
    }
}
