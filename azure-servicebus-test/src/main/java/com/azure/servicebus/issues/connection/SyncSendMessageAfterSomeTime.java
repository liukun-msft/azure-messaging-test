package com.azure.servicebus.issues.connection;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SyncSendMessageAfterSomeTime {

    public static void main(String[] args) throws InterruptedException {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(15));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .topicName(Credentials.serviceBusTopic)
                .buildClient();

        ServiceBusMessage message1 = new ServiceBusMessage("Hello world 1");
        ServiceBusMessage message2 = new ServiceBusMessage("Hello world 2");


        sender.sendMessage(message1);

        TimeUnit.MINUTES.sleep(36);

        sender.sendMessage(message2);
        sender.close();
    }
}
