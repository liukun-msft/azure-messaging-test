package com.azure.servicebus.issues;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;

import java.util.function.Consumer;


public class StartTwoProcessor {

    public static void main(String[] args) {
        String queue1 = args[0];
        String queue2 = args[1];

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println("message id:" + messageContext.getMessage().getMessageId());
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        ServiceBusClientBuilder busClientBuilder = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString);

        ServiceBusProcessorClient processorClient1 = busClientBuilder
                .processor()
                .queueName(queue1)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        ServiceBusProcessorClient processorClient2 = busClientBuilder
                .processor()
                .queueName(queue2)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        processorClient1.start();
        processorClient2.start();
    }

}

