package com.azure.servicebus.common;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;

public class SendSessionMessages {
    private static final int SEND_MESSAGE_NUMBER = 1;

    public static void main(String[] args) {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(1L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusSessionQueue)
                .buildClient();


        for (int i = 0; i < SEND_MESSAGE_NUMBER; i++) {
            ServiceBusMessage message = new ServiceBusMessage("Hello world").setMessageId("" + i).setSessionId("1");

            sender.sendMessage(message);

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
