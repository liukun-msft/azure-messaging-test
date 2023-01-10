package com.azure.servicebus.track1.common;

import com.azure.servicebus.track1.util.Credentials;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class SendMessages {

    public static void main(String[] args) {
        IMessageSender sender;
        // Create communication objects to send and receive on the queue
        try {
            sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                    new ConnectionStringBuilder(Credentials.serviceBusConnectionString, "testqueue"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
