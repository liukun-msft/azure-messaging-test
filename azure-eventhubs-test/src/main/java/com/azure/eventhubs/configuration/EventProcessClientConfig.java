package com.azure.eventhubs.configuration;

import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@Lazy
public class EventProcessClientConfig {

    @Bean
    public EventProcessorClient eventOfferUpProcessorClient() {
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(Credentials.storageConnectionString)
                .containerName(Credentials.storageContainer)
                .buildAsyncClient();

        return new EventProcessorClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME) //Need to understand consumer group
                .processEventBatch(getBatchEvents(), 2000)
                .processError(getProcessError())
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .buildEventProcessorClient();
    }

    @Bean
    public EventProcessorClient eventOfferNewProcessorClient() {

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(Credentials.storageConnectionString)
                .containerName(Credentials.storageContainer)
                .buildAsyncClient();

        return new EventProcessorClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME) //Need to understand consumer group
                .processEventBatch(getBatchEvents(), 2000)
                .processError(getProcessError())
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .buildEventProcessorClient();

    }

    private Consumer<EventBatchContext> getBatchEvents() {
        return eventBatchContext -> {
            List<EventData> events = eventBatchContext.getEvents();
            eventBatchContext.updateCheckpoint();
        };
    }

    private Consumer<ErrorContext> getProcessError() {
        return errorContext -> {
            System.out.printf("Error occurred in partition processor for partition %s, %s.%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
        };
    }
}
