package com.azure.servicebus.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Constants;
import com.azure.servicebus.util.Credentials;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("SendSimpleMessage")
public class SendSimpleMessage extends ServiceBusScenario {

    @Override
    public void run() {
        String queue = cmdlineArgs.get("queue");

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sender()
                .queueName(queue)
                .buildClient();

        List<ServiceBusMessage> messages = Arrays.asList(
                new ServiceBusMessage("Hello world").setMessageId("1"),
                new ServiceBusMessage("Bonjour").setMessageId("2"));

        sender.sendMessages(messages);

        sender.close();
    }
}
