package com.azure.servicebus.issues.longduration;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessMessages {

    public static void main(String[] args) {


        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println(messageContext.getMessage().getMessageId());

                // sleep a long duration
                TimeUnit.MINUTES.sleep(6);

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
                .processMessage(processMessage)
                .maxConcurrentCalls(2)
                .processError(processError)
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ofMinutes(60))
                .buildProcessorClient();

        processorClient.start();
    }
}
