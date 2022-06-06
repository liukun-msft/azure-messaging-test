package com.azure.eventhubs.specific;

import com.azure.core.util.IterableStream;
import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.time.Duration;

public class ConsumeThrowException {
    public static void main(String[] args) {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();

        consumer.receive().subscribe(
                event -> {
                    System.out.println("Event: " + event.getData().getBodyAsString());
                    throw new NullPointerException();
                },
                error -> {
                    System.out.println("Meet error" + error.getStackTrace());
                });
    }
}
