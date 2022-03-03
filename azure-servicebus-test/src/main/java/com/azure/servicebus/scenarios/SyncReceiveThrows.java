package com.azure.servicebus.scenarios;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service("SyncReceiveThrows")
public class SyncReceiveThrows extends ServiceBusScenario{
    private static final int NUMBER_TO_RECEIVE = 10;

    @Override
    public void run() {

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
                System.out.printf("-- Error happened while joining: %s%n", e);
                break;
            }
        }
    }

    private Thread createNewThread(int round, ServiceBusReceiverClient receiver, AtomicInteger counter) {
        return new Thread(() -> {
            System.out.println("---- CREATING " + round + " ----");
            for (ServiceBusReceivedMessage message : receiver.receiveMessages(NUMBER_TO_RECEIVE)) {
                final int i = counter.incrementAndGet();
                System.out.printf("round[%d] #[%d] messageId[%s] %n", round, i, message.getMessageId());

                if (i % 15 == 0) {
                    throw new IllegalStateException("Test error occurs. index: " + i);
                }

                try {
                    TimeUnit.MICROSECONDS.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("---> Unable to sleep. Error: " + e);
                } finally {
                    receiver.complete(message);
                }
            }
            System.out.println("---- ENDING " + round + " ----");
        });
    }
}
