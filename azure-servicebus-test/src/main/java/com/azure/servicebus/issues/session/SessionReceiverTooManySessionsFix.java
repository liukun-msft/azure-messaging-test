package com.azure.servicebus.issues.session;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.servicebus.util.Credentials;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Scanner;

public class SessionReceiverTooManySessionsFix {

    // 256 works, 257 will break the receiver
    final static Integer SessionCount = 267;

    public static void main(String[] args) throws InterruptedException {
        SessionReceiverTooManySessionsFix sample = new SessionReceiverTooManySessionsFix();

        final String sessionQueue = "sessionqueue";


        ArrayList<Disposable> subscriptions = new ArrayList<Disposable>();
        // Create a receiver.

        ArrayList<ServiceBusSessionReceiverAsyncClient> receivers = new ArrayList<>();
        ServiceBusSessionReceiverAsyncClient sessionReceiver = null;
        for (int i = 0; i < SessionCount; i++) {
            if (i % 255 == 0) {
                sessionReceiver = new ServiceBusClientBuilder()
                        .connectionString(Credentials.serviceBusConnectionString)
                        .sessionReceiver()
                        .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                        .queueName(sessionQueue)
                        .buildAsyncClient();
                receivers.add(sessionReceiver);
            }

            String sessionId = String.valueOf(i);
            System.out.println("Receiving from Session " + sessionId);

            Disposable sub = sample.run(sessionReceiver, sessionId);
            subscriptions.add(sub);
        }

        System.out.println("All session receivers active. Press any key to exit.");

        Scanner in = new Scanner(System.in);

        String s = in.nextLine();

        System.out.println("Disposing subscriptions ...");
        for (var sub : subscriptions) {
            sub.dispose();
        }

        System.out.println("Closing receiver ...");

        receivers.forEach(ServiceBusSessionReceiverAsyncClient::close);

        System.out.println("Bye.");
    }

    public Disposable run(ServiceBusSessionReceiverAsyncClient sessionReceiver, String sessionId) {

        try {
            Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptSession(sessionId);

            System.out.println("-- Session " + sessionId + " accepted.");

            Disposable subscription = Flux.usingWhen(receiverMono,
                            receiver -> receiver.receiveMessages(),
                            receiver -> Mono.fromRunnable(() -> receiver.close()))
                    .subscribe(message -> {
                        System.out.printf("Session: %s. Sequence #: %s. Contents: %s%n", message.getSessionId(),
                                message.getSequenceNumber(), message.getBody());

                    }, error -> {
                        System.err.println("Error occurred: " + error);
                    });

            return subscription;
        } catch (Exception ex) {
            System.err.println("Error accepting session " + sessionId + ": " + ex);
            return null;
        }
    }
}
