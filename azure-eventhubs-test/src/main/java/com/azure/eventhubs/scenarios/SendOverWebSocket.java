package com.azure.eventhubs.scenarios;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.eventhubs.util.Credentials;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service("SendOverWebSocket")
public class SendOverWebSocket extends EventHubsScenario{
    @Override
    public void run() {
        final String proxyAddress = cmdlineArgs.get("address");
        final int proxyPort = Integer.parseInt(cmdlineArgs.get("port"));

        ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC,
                new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyAddress, proxyPort)),
                Credentials.proxyUserName, Credentials.proxyPassword);

        EventHubProducerClient producer = new EventHubClientBuilder()
                .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
                .proxyOptions(proxyOptions)
                .connectionString(Credentials.eventHubsConnectionString, Credentials.eventHub)
                .buildProducerClient();

        sendData(producer);

        producer.close();
    }

    private void sendData(EventHubProducerClient producer){
        EventDataBatch currentBatch = producer.createBatch();
        List<EventData> events = Arrays.asList(
                new EventData("Roast beef".getBytes(UTF_8)),
                new EventData("Cheese".getBytes(UTF_8)),
                new EventData("Tofu".getBytes(UTF_8)),
                new EventData("Turkey".getBytes(UTF_8)));

        for (EventData event : events) {
            if (currentBatch.tryAdd(event)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            producer.send(currentBatch);
            currentBatch = producer.createBatch();

            // Add that event that we couldn't before.
            if (!currentBatch.tryAdd(event)) {
                System.err.printf("Event is too large for an empty batch. Skipping. Max size: %s. Event: %s%n",
                        currentBatch.getMaxSizeInBytes(), event.getBodyAsString());
            }
        }
    }
}
