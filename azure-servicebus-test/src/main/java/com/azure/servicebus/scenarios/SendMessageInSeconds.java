package com.azure.servicebus.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.CmdlineArgs;
import com.azure.servicebus.util.Credentials;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("SendMessageInSeconds")
public class SendMessageInSeconds extends ServiceBusScenario {

    @Override
    public void run() {
        String queue = cmdlineArgs.get("queue");

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sender()
                .queueName(queue)
                .buildClient();

        List<ServiceBusMessage> messages = Arrays.asList(
                new ServiceBusMessage("Hello world").setMessageId("1"));

        for(int i = 0; i < 300; i++){
            sender.sendMessages(messages);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
