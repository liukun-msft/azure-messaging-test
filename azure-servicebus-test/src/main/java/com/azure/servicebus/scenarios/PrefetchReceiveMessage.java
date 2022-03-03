package com.azure.servicebus.scenarios;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.servicebus.util.Credentials;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service("PrefetchReceiveMessage")
public class PrefetchReceiveMessage  extends ServiceBusScenario{
    private static final int NUMBER_TO_RECEIVE = 10;

    @Override
    public void run() {
        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        try {
            int count = 1;
            for(int i = 0; i < 3; i++){
                IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(NUMBER_TO_RECEIVE);
                for (ServiceBusReceivedMessage message : messages) {
                    System.out.printf("count[%s] messageId[%s] %n", count, message.getMessageId());
                    count++;
                    receiver.complete(message);
                    System.out.println("Complete message");
                }
                System.out.println("---- ENDING  ----");
            }
        } catch (Exception e){
            System.out.println(e);
        }


        receiver.close();
    }
}
