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
    private static final CountDownLatch latch = new CountDownLatch(1);

    private static final int PREFETCH_COUNT = 0;
    private static final int MAX_CONCURRENT_CALLS = 10;
    private static final int MAX_LOCK_RENEW_DURATION_IN_MINUTES = 0;
    private static final int PROCESS_TIME_IN_SECONDS = 1;

    public static void main(String[] args) throws InterruptedException {
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.printf("Thread name [%s] - received message id [%s], message sequence number [%s] " +
                                ",  message token [%s], message content [%s] \n",
                        Thread.currentThread().getName(),
                        messageContext.getMessage().getMessageId(),
                        messageContext.getMessage().getSequenceNumber(),
                        messageContext.getMessage().getLockToken(),
                        messageContext.getMessage().getBody());

                // other message processing code
                TimeUnit.SECONDS.sleep(PROCESS_TIME_IN_SECONDS);

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
                .queueName(Credentials.serviceBusQueue)
                .prefetchCount(PREFETCH_COUNT)
                .maxConcurrentCalls(MAX_CONCURRENT_CALLS)
                .maxAutoLockRenewDuration(Duration.ofMinutes(MAX_LOCK_RENEW_DURATION_IN_MINUTES))
                .disableAutoComplete()
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        processorClient.start();

        latch.await();
    }
}
