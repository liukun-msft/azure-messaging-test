package com.azure.eventhubs.scenarios;

import com.azure.messaging.eventhubs.EventProcessorClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service("ProcessBatchEventsWithTwoProcessorClient")
public class ProcessBatchEventsWithTwoProcessorClient extends EventHubsScenario {

    @Autowired
    EventProcessorClient eventOfferUpProcessorClient;
    @Autowired
    EventProcessorClient eventOfferNewProcessorClient;

    @Override
    public void run() {
        eventOfferUpProcessorClient.start();
        eventOfferNewProcessorClient.start();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        eventOfferNewProcessorClient.stop();
        eventOfferUpProcessorClient.stop();

    }
}
