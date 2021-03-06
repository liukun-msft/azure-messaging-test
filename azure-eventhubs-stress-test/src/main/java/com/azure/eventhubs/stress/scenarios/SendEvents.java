package com.azure.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.azure.eventhubs.stress.util.Constants.EVENT_HUBS_CONNECTION_STRING;
import static com.azure.eventhubs.stress.util.Constants.EVENT_HUB_NAME;

@Service("SendEvents")
public class SendEvents extends EventHubsScenario {
    private final int sendTimes = 1000;
    private final int eventNumber =  500;

    @Override
    public void run() {
        String eventHubConnStr = options.get(EVENT_HUBS_CONNECTION_STRING);
        String eventHub = options.get(EVENT_HUB_NAME);

        EventHubProducerAsyncClient client = new EventHubClientBuilder()
                .connectionString(eventHubConnStr, eventHub)
                .buildAsyncProducerClient();

        Flux.range(0, sendTimes).concatMap(i -> {
            List<EventData> eventDataList = new ArrayList<>();
            IntStream.range(0, eventNumber).forEach(j -> {
                eventDataList.add(new EventData("A"));
            });
            return client.send(eventDataList);
        }).subscribe();
    }
}
