package com.azure.servicebus.issues.abandon;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SessionProcessorAlwaysComplete {
    private static Logger log = LoggerFactory.getLogger(SessionProcessorMultipleSessions.class);

    public static void main(String[] args) {
        Consumer<ServiceBusReceivedMessageContext> onMessage = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            Order order = null;
            try {

                order = new Order(message.getBody().toString());
                log.info("Processing message. Session: {}, Sequence #: {}, orderNo: {}, message id: {} ",
                        message.getSessionId(), message.getSequenceNumber(), order.orderNo, message.getMessageId());
                context.complete();
            } catch (Exception e) {
                log.error("Error in objectmapper", e);
                context.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> onError = context -> {
            System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                    context.getFullyQualifiedNamespace(), context.getEntityPath());
            if (context.getException() instanceof ServiceBusException) {
                ServiceBusException exception = (ServiceBusException) context.getException();
                System.out.printf("Error source: %s, reason %s%n", context.getErrorSource(),
                        exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", context.getException());
            }
        };

        ServiceBusProcessorClient sessionProcessor = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sessionProcessor()
                .prefetchCount(0)
                .queueName(Credentials.serviceBusQueue)
                .maxConcurrentSessions(1)
                .maxConcurrentCalls(2)
                .processMessage(onMessage)
                .processError(onError)
                .disableAutoComplete()
                .buildProcessorClient();


        // Start the processor in the background
        sessionProcessor.start();
    }
}
