package com.azure.servicebus.common;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class ScheduleMessage {
    private static final int SEND_MESSAGE_NUMBER = 10;
    private static final int SCHEDULE_GAP_IN_SECONDS = 30;

    public static void main(String[] args) throws InterruptedException {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(1L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();


        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            ServiceBusMessage messages = new ServiceBusMessage("Hello world").setMessageId("" + i);

            OffsetDateTime scheduleTime = OffsetDateTime.now().plusSeconds(30);
            sender.scheduleMessage(messages, scheduleTime);

            System.out.println("Send Message id: " + i);

            System.out.printf("Sleep [%s] seconds \n", SCHEDULE_GAP_IN_SECONDS);
            TimeUnit.SECONDS.sleep(SCHEDULE_GAP_IN_SECONDS);
        }

        sender.close();
    }
}
