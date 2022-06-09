package com.azure.eventhubs.common;

import com.azure.core.util.IterableStream;
import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ConsumeEvents {
    public static void main(String[] args) throws InterruptedException {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();

        consumer.receive().subscribe(
                event -> {
                    System.out.println("Event: " + event.getData().getBodyAsString());
                },
                error -> {
                    System.out.println("Meet error" + error);
                }
        );

        TimeUnit.SECONDS.sleep(10);

        consumer.close();
    }
}
