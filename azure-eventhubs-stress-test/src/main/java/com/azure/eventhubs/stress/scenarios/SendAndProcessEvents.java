package com.azure.eventhubs.stress.scenarios;

import com.azure.eventhubs.stress.util.Constants;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service("SendAndProcessEvents")
public class SendAndProcessEvents extends EventHubsScenario{

    private static final int BATCH_NUMBER = 1000;
    private static final int EVENTS_NUMBER_EACH_BATCH = 500;

    @Override
    public void run() {
        sendEvents().subscribe();

        String storageConnectionString = options.get(Constants.STORAGE_CONNECTION_STRING);
        String storageContainer = options.get(Constants.STORAGE_CONTAINER_NAME);

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName(storageContainer)
                .buildAsyncClient();

        String eventHubsConnectionString = options.get(Constants.EVENT_HUBS_CONNECTION_STRING);
        String eventHubName = options.get(Constants.EVENT_HUB_NAME);

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(eventHubsConnectionString, eventHubName)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                })
                .processError(errorContext -> {
                    System.out.println("Error occurred " + errorContext.getThrowable().getMessage());
                })
                .buildEventProcessorClient();

        eventProcessorClient.start();
    }

    private Flux<Void> sendEvents(){
        String eventHubsConnectionString = options.get(Constants.EVENT_HUBS_CONNECTION_STRING);
        String eventHubName = options.get(Constants.EVENT_HUB_NAME);

        EventHubProducerAsyncClient client = new EventHubClientBuilder()
                .connectionString(eventHubsConnectionString, eventHubName)
                .buildAsyncProducerClient();

        Scheduler scheduler = Schedulers.newSingle("send-message");

        return Flux.range(0, BATCH_NUMBER).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, EVENTS_NUMBER_EACH_BATCH).forEach(j -> {
                eventDataList.add(new EventData("A"));
            });
            return client.send(eventDataList);
        }).subscribeOn(scheduler);
    }


}
