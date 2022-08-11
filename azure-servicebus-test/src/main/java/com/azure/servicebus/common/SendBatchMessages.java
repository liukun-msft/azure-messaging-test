package com.azure.servicebus.common;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SendBatchMessages {

    private static final int SEND_BATCH_NUMBER = 100;
    private static final int BATCH_MESSAGE_NUMBER = 100;

    public static void main(String[] args) {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(1L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();


        for (int i = 0; i < SEND_BATCH_NUMBER; i++) {
            List<ServiceBusMessage> messages = new ArrayList<>();
            for(int j = 0; j < BATCH_MESSAGE_NUMBER; j++){
                messages.add(new ServiceBusMessage("Hello world").setMessageId("" + i * BATCH_MESSAGE_NUMBER + j));
            }

            sender.sendMessages(messages);

            System.out.println("Send Message times: " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
