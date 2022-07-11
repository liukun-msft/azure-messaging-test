package com.azure.servicebus.issues.connection;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SendMessageAfterSomeTime {


    public static void main(String[] args) {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofSeconds(5));


        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .topicName(Credentials.serviceBusTopic)
                .buildAsyncClient();

        ServiceBusMessage message1 = new ServiceBusMessage("Hello world 1");
        ServiceBusMessage message2 = new ServiceBusMessage("Hello world 2");


        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Close network to fail the first message delivery
        try {
            sender.sendMessage(message1).block();
        } catch (Exception e) {
            System.out.println(e);
        }

        //Wait 30 minutes
        try {
            TimeUnit.MINUTES.sleep(31);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sender.sendMessage(message2).block();
        sender.close();
    }
}
