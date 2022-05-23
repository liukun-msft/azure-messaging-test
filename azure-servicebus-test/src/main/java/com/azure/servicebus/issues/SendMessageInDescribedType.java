package com.azure.servicebus.issues;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.implementation.UriDescribedType;
import com.azure.servicebus.util.Credentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

public class SendMessageInDescribedType {
    private static final int SEND_MESSAGE_NUMBER = 1;

    public static void main(String[] args) throws URISyntaxException {
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofMinutes(5L));

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();


        for(int i = 0; i < SEND_MESSAGE_NUMBER; i++){
            AmqpMessageBody body = AmqpMessageBody.fromValue(new UriDescribedType(new URI("http://example.com")));

            //\x00Sq\xc1\x01\x00\x00Sr\xc1\x01\x00\x00Ss\xd0\x00\x00\x00\x09\x00\x00\x00\x02\xa1\x010\xa0\x00\x00St\xc1\x01\x00\x00Sw\x00\xa3\x11com.microsoft:uri\xa1\x12http://example.com\x00Sx\xc1\x01\x00

            List<ServiceBusMessage> messages = List.of(
                    new ServiceBusMessage(body).setMessageId("" + i));

            sender.sendMessages(messages);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
