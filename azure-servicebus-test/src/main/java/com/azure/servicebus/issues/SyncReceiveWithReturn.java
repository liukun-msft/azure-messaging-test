package com.azure.servicebus.issues;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class SyncReceiveWithReturn {
    private static final int NUMBER_TO_RECEIVE = 10;

    public static void main(String[] args) {
        final AtomicInteger counter = new AtomicInteger();

        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        int rounds = 100;
        for (int i = 0; i < rounds; i++) {
            final Thread thread = createNewThread(i, receiver, counter);
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                thread.interrupt();
                System.out.printf("-- Error happened while joining: %s%n", e);
                break;
            }

        }
    }


    private static Thread createNewThread(int round, ServiceBusReceiverClient receiver, AtomicInteger counter) {
        return new Thread(() -> {
            System.out.println("---- CREATING " + round + " ----");
            for (ServiceBusReceivedMessage message : receiver.receiveMessages(NUMBER_TO_RECEIVE)) {
                final int i = counter.incrementAndGet();
                System.out.printf("round[%d] #[%d] messageId[%s] %n", round, i, message.getMessageId());

                if (i % 12 == 0) {
                    System.out.println("Stop iterator by break");
                    break;
                }
                receiver.complete(message);
            }
            System.out.println("---- ENDING " + round + " ----");
        });
    }
}
