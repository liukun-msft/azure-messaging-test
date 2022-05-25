package com.azure.servicebus.issues;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.List;

public class SendMessageInTransaction {
    private static final int SEND_MESSAGE_NUMBER = 1;

    public static void main(String[] args) {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(5L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();
        ServiceBusTransactionContext context = sender.createTransaction();


        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage("Hello world").setMessageId("" + i));

            sender.sendMessages(messages, context);

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
