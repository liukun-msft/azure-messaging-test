package com.azure.eventhubs.common;

import com.azure.core.util.IterableStream;
import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.time.Duration;

public class ConsumeOneEventSync {
    public static void main(String[] args) {
        EventHubConsumerClient consumer = new EventHubClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildConsumerClient();

        IterableStream<PartitionEvent> events = consumer.receiveFromPartition("0", 1,
                EventPosition.earliest(), Duration.ofSeconds(40));
        for (PartitionEvent event : events) {
            System.out.println("Event: " + event.getData().getBodyAsString());
        }

        consumer.close();
    }
}
