package com.azure.servicebus.common;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessMessages {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println(messageContext.getMessage().getMessageId());
                // other message processing code
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .processor()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .disableAutoComplete()
                .queueName(Credentials.serviceBusQueue)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        processorClient.start();

        latch.await();
    }
}
