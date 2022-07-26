package com.azure.servicebus.issues.abandon;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.List;

public class SendOrderMultipleSessions {
    private static final int SEND_MESSAGE_NUMBER = 10;
    private static final int SESSION_NUMBER = 3;

    public static void main(String[] args) {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(1L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();


        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage("" + i).setMessageId("" + i).setSessionId("test1"));

            sender.sendMessages(messages);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage("" + i).setMessageId("" + i).setSessionId("test2"));

            sender.sendMessages(messages);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage("" + i).setMessageId("" + i).setSessionId("test2"));

            sender.sendMessages(messages);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sender.close();
    }
}
