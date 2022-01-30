package com.azure.servicebus.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("SendSimpleMessage")
public class SendSimpleMessage extends ServiceBusScenario {

    @Override
    public void run() {
        String connectionString = System.getenv(Constants.AZURE_SERVICE_BUS_CONNECTION_STRING);
        String queueName = System.getenv(Constants.AZURE_SERVICE_BUS_QUEUE);

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();

        List<ServiceBusMessage> messages = Arrays.asList(
                new ServiceBusMessage("Hello world").setMessageId("1"),
                new ServiceBusMessage("Bonjour").setMessageId("2"));

        sender.sendMessages(messages);

        sender.close();
    }
}
