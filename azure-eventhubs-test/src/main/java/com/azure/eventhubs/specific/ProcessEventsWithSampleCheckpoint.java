package com.azure.eventhubs.specific;

import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;

import java.util.concurrent.TimeUnit;

public class ProcessEventsWithSampleCheckpoint {
    public static void main(String[] args) throws InterruptedException {

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .checkpointStore(new SampleCheckpointStore())
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                    eventContext.updateCheckpoint();
                })
                .processError(errorContext -> {
                    System.out
                            .println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
                })
                .buildEventProcessorClient();

// This will start the processor. It will start processing events from all partitions.
        eventProcessorClient.start();

// (for demo purposes only - adding sleep to wait for receiving events)
        TimeUnit.SECONDS.sleep(2);

// This will stop processing events.
        eventProcessorClient.stop();
    }
}
