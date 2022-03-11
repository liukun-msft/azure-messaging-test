package com.azure.servicebus.issues;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncReceiveInMultipleThreads {

    private static final int NUMBER_TO_RECEIVE = 10;
    private static final int THREAD_NUMBER = 10;

    public static void main(String[] args) {
        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        Executor executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        for (int i = 0; i < THREAD_NUMBER; i++) {
            final int number = i;
            executor.execute(() -> receiveMessage(number, receiver));
        }
    }


    private static void receiveMessage(int round, ServiceBusReceiverClient receiver) {
        for (ServiceBusReceivedMessage message : receiver.receiveMessages(NUMBER_TO_RECEIVE)) {
            System.out.printf("round[%d] messageId[%s] %n", round, message.getMessageId());
            try {
                TimeUnit.MICROSECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("---> Unable to sleep. Error: " + e);
            } finally {
                receiver.complete(message);
            }
        }
    }


}
