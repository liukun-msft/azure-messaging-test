package com.azure.servicebus.issues;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class OneReceiverInOneThread {

    private static final int NUMBER_TO_RECEIVE = 10;

    public static void main(String[] args) {
        int rounds = 100;
        for (int i = 0; i < rounds; i++) {
            final Thread thread = createNewThread(i);
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.printf("-- Error happened while joining: %s%n", e);
                break;
            }

        }
    }


    private static Thread createNewThread(int round) {

        return new Thread(() -> {
            //For each thread create one receiver to receive
            ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                    .connectionString(Credentials.serviceBusConnectionString)
                    .receiver()
                    .disableAutoComplete()
                    .maxAutoLockRenewDuration(Duration.ZERO)
                    .queueName(Credentials.serviceBusQueue)
                    .buildClient();

            for (ServiceBusReceivedMessage message : receiver.receiveMessages(NUMBER_TO_RECEIVE)) {
                System.out.printf("round[%d] messageId[%s] %n", round, message.getMessageId());

                try {
                    TimeUnit.MICROSECONDS.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Unable to sleep. Error: " + e);
                } finally {
                    receiver.complete(message);
                }
            }
        });
    }
}
