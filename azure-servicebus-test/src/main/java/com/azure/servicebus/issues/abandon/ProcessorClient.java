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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessorClient {
    private static Logger log = LoggerFactory.getLogger(ProcessorClient.class);

    public static void main(String[] args) {
        Consumer<ServiceBusReceivedMessageContext> onMessage = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            Order order = null;
            try {

                order = new Order(message.getBody().toString());
                log.info("Processing message. Session: {}, Sequence #: {}, orderNo: {}, message id: {} ",
                        message.getSessionId(), message.getSequenceNumber(), order.orderNo, message.getMessageId());
                //abandoning orders have ids as multiple of 4
                if (order.getOrderNo() % 4 == 0) {
                    TimeUnit.SECONDS.sleep(5);
                    context.abandon();
                } else {
                    context.complete();
                }
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

        ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .processor()
                .prefetchCount(0)
                .queueName(Credentials.serviceBusQueue)
                .processMessage(onMessage)
                .processError(onError)
                .disableAutoComplete()
                .buildProcessorClient();

        // Start the processor in the background
        processor.start();
    }
}
