package com.azure.servicebus.issues.connection;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.servicebus.util.Credentials;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessMessageAfterSomeTime {

    public static void main(String[] args) throws InterruptedException {
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println(messageContext.getMessage().getMessageId());
                // other message processing code
                TimeUnit.MINUTES.sleep(16);
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofSeconds(5));

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .processor()
                .queueName(Credentials.serviceBusQueue)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        processorClient.start();
    }

}
