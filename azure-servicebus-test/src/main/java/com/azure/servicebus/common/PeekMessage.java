package com.azure.servicebus.common;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;

public class PeekMessage {
    private static final int NUMBER_TO_PEEK = 10;

    public static void main(String[] args) {

        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .receiver()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        try {
            int count = 1;
            for (int i = 0; i < 1; i++) {
                IterableStream<ServiceBusReceivedMessage> messages = receiver.peekMessages(NUMBER_TO_PEEK);
                for (ServiceBusReceivedMessage message : messages) {
                    System.out.printf("count[%s] messageId[%s] %n", count, message.getMessageId());
                    count++;
                }
                System.out.println("---- ENDING  ----");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        receiver.close();
    }
}
