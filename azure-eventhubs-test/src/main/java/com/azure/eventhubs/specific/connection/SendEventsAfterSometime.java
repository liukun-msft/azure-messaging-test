package com.azure.eventhubs.specific.connection;

import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SendEventsAfterSometime {

    private final static int EVENT_NUMBER = 1;

    public static void main(String[] args) throws InterruptedException {

        EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .buildProducerClient();

        EventDataBatch batch = producer.createBatch();
        List<EventData> allEvents = new ArrayList<>();
        for (int i = 0; i < EVENT_NUMBER; i++) {
            allEvents.add(new EventData("Foo " + i));
        }

        for (EventData eventData : allEvents) {
            if (!batch.tryAdd(eventData)) {
                producer.send(batch);
                batch = producer.createBatch();

                // Try to add that event that couldn't fit before.
                if (!batch.tryAdd(eventData)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                            + batch.getMaxSizeInBytes());
                }
            }
        }

        producer.send(batch);

        TimeUnit.MINUTES.sleep(37);

        producer.send(batch);
        producer.close();
    }
}
