package com.azure.servicebus.issues.schedule;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ReceiveAndScheduleMessage {
    private static ServiceBusSenderClient sender;
    private static ServiceBusProcessorClient processor;
    private static int scheduleTimes = 0;

    private static void scheduleMessage() {
        System.out.println("Start schedule message.");
        ServiceBusMessage messages = new ServiceBusMessage("Scheduled Message");
        scheduleTimes++;

        OffsetDateTime scheduleTime = OffsetDateTime.now().plusSeconds(30);
        sender.scheduleMessage(messages, scheduleTime);
        System.out.println("Sent scheduled message.");
    }

    private static final Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
        System.out.println("Received message with id: " + messageContext.getMessage().getMessageId());

        try {
            // Scenario 1: sender schedule message first, then receiver renew lock for second message
            // Create sender's management channel first, then create receiver's management channel
            if (scheduleTimes >= 1) {
                TimeUnit.SECONDS.sleep(30);
            }
            scheduleMessage();

            // Scenario 2: renew lock for first message
            // Create receiver's management channel first, then create sender's management channel
//            TimeUnit.SECONDS.sleep(30);
//            scheduleMessage();

            // Scenario 3: sender schedule message first, then receiver renew lock before complete message
            // Create receiver's management channel first, then create sender's management channel
//            scheduleMessage();
//            TimeUnit.SECONDS.sleep(30);

        } catch (Exception e) {
            e.printStackTrace();
        }

        messageContext.complete();

        System.out.println("Finished process message.");
    };

    private static final Consumer<ServiceBusErrorContext> processError = errorContext -> {
        System.err.println("Error occurred while receiving message: " + errorContext.getException());
    };


    public static void main(String[] args) {
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString);

        sender = builder.sender()
                .topicName(Credentials.serviceBusQueue)
                .buildClient();

        processor = builder
                .processor()
//                .maxConcurrentCalls(2)
                .disableAutoComplete()
                .queueName(Credentials.serviceBusQueue)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        processor.start();
    }
}
