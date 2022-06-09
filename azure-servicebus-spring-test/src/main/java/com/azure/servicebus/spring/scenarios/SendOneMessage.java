package com.azure.servicebus.spring.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.spring.util.Credentials;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("SendOneMessage")
public class SendOneMessage extends ServiceBusScenario {

    @Override
    public void run() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        List<ServiceBusMessage> messages = Arrays.asList(
                new ServiceBusMessage("Hello world").setMessageId("1"),
                new ServiceBusMessage("Bonjour").setMessageId("2"));

        sender.sendMessages(messages);

        sender.close();
    }
}
