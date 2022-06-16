package com.azure.servicebus.issues.order;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.servicebus.util.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiverAbandonMessage {
    private static Logger log = LoggerFactory.getLogger(ReceiverAbandonMessage.class);

    public static void main(String[] args) {
        ServiceBusSessionReceiverClient sessionReceiverClient = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .sessionReceiver()
                .disableAutoComplete()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        while (true) {
            ServiceBusReceiverClient sessionReceiver = sessionReceiverClient.acceptNextSession();
            sessionReceiver.receiveMessages(1).forEach(message -> {
                try {
                    Order order = new Order(message.getBody().toString());
                    log.info("Processing message. Session: {}, Sequence #: {}, orderNo: {}", message.getSessionId(), message.getSequenceNumber(), order.orderNo);
                    if (order.orderNo % 4 == 0) {
                        sessionReceiver.abandon(message);
                    } else {
                        sessionReceiver.complete(message);
                    }
                } catch (Exception e) {
                    log.error("Error occur: ", e);
                    sessionReceiver.abandon(message);
                }
            });
            sessionReceiver.close();
        }
    }
}
