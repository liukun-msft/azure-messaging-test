package com.azure.servicebus.track1.common;

import com.azure.servicebus.track1.util.Credentials;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import java.time.Duration;

public class ReceiveSessionMessages {
    public static void main(String[] args) {
        IMessageReceiver receiver;

        try {
            receiver = ClientFactory.acceptSessionFromConnectionString(
                    Credentials.serviceBusConnectionString, "sessionqueue", ReceiveMode.PEEKLOCK);

            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            receiver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
