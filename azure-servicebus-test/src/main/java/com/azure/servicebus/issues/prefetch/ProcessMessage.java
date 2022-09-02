package com.azure.servicebus.issues.prefetch;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessMessage {
    private static final ClientLogger log = new ClientLogger(ProcessMessage.class);

    public static void main(String[] args) {
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            try {
                System.out.println("Received message with id: " + context.getMessage().getMessageId());

                TimeUnit.MINUTES.sleep(20);
                context.complete();

                System.out.println("Consume message successfully");
            } catch (Exception e) {
                System.err.println("Consume message failure!" + e);
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .processor()
                .prefetchCount(3)
                .maxConcurrentCalls(3)
                .disableAutoComplete()
                .queueName(Credentials.serviceBusQueue)
                .maxAutoLockRenewDuration(Duration.ofMinutes(5))
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        processorClient.start();
    }
}
