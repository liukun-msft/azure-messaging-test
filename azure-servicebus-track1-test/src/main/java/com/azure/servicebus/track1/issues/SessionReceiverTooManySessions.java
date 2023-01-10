package com.azure.servicebus.track1.issues;

import com.azure.servicebus.track1.util.Credentials;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import java.time.Duration;
import java.util.Scanner;

public class SessionReceiverTooManySessions {

    // 256 works, 257 will break the receiver
    final static int SessionCount = 267;

    public static void main(String[] args) throws Exception {

        for (int i = 0; i < SessionCount; i++) {
            String sessionId = Integer.toString(i);
            System.out.println("Receiving from Session " + sessionId);

            IMessageReceiver receiver = ClientFactory.acceptSessionFromConnectionStringBuilder(
                    new ConnectionStringBuilder(Credentials.serviceBusConnectionString, "sessionqueue"), sessionId, ReceiveMode.PEEKLOCK);

            var receivedMessage = receiver.receiveAsync(Duration.ofSeconds(1));
        }

        System.out.println("All session receivers active. Press any key to exit.");

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();

        System.out.println("Bye.");
    }
}
