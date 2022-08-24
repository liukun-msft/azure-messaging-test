package com.azure.servicebus.issues.schedule;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ReceiveAndScheduleMessage {
    private static ServiceBusSenderClient sender;
    private static ServiceBusClientBuilder builder;
    private static int scheduleTimes = 0;

    public static void main(String[] args) {

        builder = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString);

        startProcessor();
    }

    private static void buildSender() {
        sender = builder.sender()
                .topicName(Credentials.serviceBusQueue)
                .buildClient();


    }

    private static void scheduleMessage() {
        ServiceBusMessage messages = new ServiceBusMessage("Hello world");
        scheduleTimes++;

        OffsetDateTime scheduleTime = OffsetDateTime.now().plusSeconds(40);
        sender.scheduleMessage(messages, scheduleTime);
        System.out.println("Send Message");
    }

    private static void startProcessor() {
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            System.out.println(messageContext.getMessage().getMessageId());
            try {
                if (scheduleTimes < 1) {
                    TimeUnit.SECONDS.sleep(1);
                    scheduleMessage();
                    TimeUnit.SECONDS.sleep(31);
                } else {
                    TimeUnit.SECONDS.sleep(31);
                    scheduleMessage();
                    TimeUnit.SECONDS.sleep(1);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            messageContext.complete();

        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        buildSender();

        ServiceBusProcessorClient processorClient = builder
                .processor()
//                .maxConcurrentCalls(2)
                .disableAutoComplete()
                .queueName(Credentials.serviceBusQueue)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();

        processorClient.start();
    }
}
