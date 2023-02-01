package com.azure.servicebus.common;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.Scanner;
import java.util.function.Consumer;

public class ProcessSessionMessages {

    public static void main(String[] args) {
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

        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofSeconds(30));

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .retryOptions(options)
                .connectionString(Credentials.serviceBusConnectionString)
                .sessionProcessor()
                .queueName(Credentials.serviceBusSessionQueue)
                .processMessage(processMessage)
                .processError(processError)
                .maxConcurrentSessions(1)
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ofSeconds(0))
                .buildProcessorClient();

        processorClient.start();

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();

        System.out.println("close processor client ...");

        processorClient.close();

        System.out.println("start processor client again...");

        processorClient.start();

        String s2 = in.nextLine();

        System.out.println("close processor client ...");

        processorClient.close();

    }
}
