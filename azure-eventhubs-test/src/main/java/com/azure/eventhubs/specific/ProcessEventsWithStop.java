package com.azure.eventhubs.specific;

import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.util.concurrent.TimeUnit;

public class ProcessEventsWithStop {
    public static void main(String[] args) throws InterruptedException {
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(Credentials.storageConnectionString)
                .containerName(Credentials.storageContainer)
                .buildAsyncClient();


        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                })
                .processError(errorContext -> {
                    System.out
                            .println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
                })
                .buildEventProcessorClient();


        eventProcessorClient.start();

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eventProcessorClient.stop();
    }
}
