package com.azure.servicebus.issues;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.function.Consumer;

public class ReceiveMessageInTransaction {
    private static final int NUMBER_TO_RECEIVE = 2;

    public static void main(String[] args) {

        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(NUMBER_TO_RECEIVE);
        ServiceBusTransactionContext transaction = receiver.createTransaction();

        for (ServiceBusReceivedMessage message : messages) {

            // Process messages and associate operations with the transaction.
//            ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage(message.getSequenceNumber());
            receiver.complete(message, new CompleteOptions().setTransactionContext(transaction));
//            receiver.abandon(message, new AbandonOptions().setTransactionContext(transaction));
        }

        receiver.commitTransaction(transaction);
        receiver.close();
    }
}

