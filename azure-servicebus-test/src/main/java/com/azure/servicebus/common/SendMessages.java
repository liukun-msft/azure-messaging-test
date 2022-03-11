package com.azure.servicebus.common;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.util.List;

public class SendMessages {

    private static final int SEND_MESSAGE_NUMBER = 40;

    public static void main(String[] args) {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();


        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage("Hello world").setMessageId("" + i));

            sender.sendMessages(messages);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
