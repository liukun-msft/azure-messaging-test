package com.azure.eventhubs.scenarios;


import com.azure.eventhubs.scenarios.EventHubsScenario;
import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;


@Service("SendAsync")
public class SendAsync extends EventHubsScenario {

    public void run() {
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .buildAsyncProducerClient();

        sendData(producer);
        sendData(producer);
        sendData(producer);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        producer.close();
    }

    private void sendData(EventHubProducerAsyncClient producer){
        producer.createBatch().flatMap(batch -> {
            batch.tryAdd(new EventData("Roast beef".getBytes(UTF_8)));
            return producer.send(batch);
        }).subscribe(data -> System.out.println("send data" + data),
                error -> System.err.println("Error occur " + error),
                () -> System.out.println("Send complete!"));
    }

}
