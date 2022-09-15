// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.servicebus.issues.credit;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;
import sun.tools.jconsole.CreateMBeanDialog;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Four {
    //   The Three.java demonstrated when we combine maxConcurrentCalls=1 with removal
    //   of one unnecessary thread-Hoping (as below), the Processor get stuck.
    //
    //   ReceiveLinkHandler (buffering@Sinks.Many ✓)
    //    |
    //    |-- ServiceBusReactorReceiver (buffering@PublishOnA ✓)
    //         |
    //         |-- ServiceBusReceiveLinkProcessor (buffering@PublishOnB x)
    //              |
    //              |-- Consumer
    //
    //  The Three.java also explains the reason for such a hang is a deadlock.
    //
    //  One observation here is, the backpressure from the downstream is applied
    //  for both Delivery-Message and Delivery-Disposition-Ack. But the backpressure
    //  (derived from the credit) should impact only Delivery-Message(s), not
    //  Delivery-Disposition-Ack(s).
    //
    //   In Four.java, for prototyping purpose, the backpressure can be limited only
    //   to Delivery-Message(s) by setting an environment variable, it will have
    //   Delivery-Disposition-Ack(s) to stream separately. The new flow become:
    //
    //   ReceiveLinkHandler (buffering1a@Sinks.Many for Delivery-Message,buffering1b@Sinks.Many for Delivery-Disposition-Ack ✓)
    //    |     \                           \
    //    |      \ Delivery-Message(s)       \----- Delivery-Disposition-Ack(s)
    //    |       \
    //    |-- ServiceBusReactorReceiver (buffering2@PublishOn ✓)
    //         |    \
    //         |     \ Delivery-Message(s)
    //         |      \
    //         |-- ServiceBusReceiveLinkProcessor (buffering3@PublishOn x)
    //              |   \
    //              |    \ Delivery-Message(s)
    //              |     \
    //              |-- Consumer
    //
    //  Run-Setup
    //  =========
    //  |--------------------|----------------------------------------------------|
    //  |   Processor Setup: |  .prefetchCount(80)                                |
    //  |                    |  .maxConcurrentCalls(1)                            |
    //  |------------------- |----------------------------------------------------|
    //  |  JVM Option        |  -Dreactor.schedulers.defaultBoundedElasticSize=2  |
    //  |--------------------|----------------------------------------------------|
    //  |   SDK Code tweak   |  One Thread-Hoping must be disabled and ack        |
    //  |                    |  streaming should be enabled via env-vars                          |
    //  |                    |                                                    |
    //  |                    |  PUB_ON_IN_SB_LINK_PROCESSOR_OFF=1                 |
    //  |                    |  STREAM_DISPOSITION_ACKS=1                         |
    //  |--------------------|----------------------------------------------------|
    //
    //  With backpressure is fixed to apply only for Delivery-Message(s), the Processor
    //  no longer enter into a deadlock.
    //
    //  When running Four.java, one thing you'll observe is ReceiveLinkHandler's buffer
    //  is overflowing, leading recreation of ReceiveLink. This is because there is a
    //  bug in credit calculation, see Five.java.
    //
    //  Learnings:
    //  ==========
    //      1. We have identified that backpressure is impacting all Deliveries types, i.e.
    //         both Message and Disposition-Ack Deliveries, ideally it should impact
    //         only Message Deliveries.
    //      2. We also observed that there is a buffer overflow in ReceiveLinkHandler.
    //
    public static void main(String[] args) throws InterruptedException {

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                // Sleep 1 second to mock process progress
                TimeUnit.SECONDS.sleep(1);
                System.out.println(Thread.currentThread().getName() + " [begin]: " + messageContext.getMessage().getLockToken());
                messageContext.complete();
                System.out.println(Thread.currentThread().getName() + " [end]: " + messageContext.getMessage().getLockToken());
            } catch (Exception ex) {
                System.out.println("\n\n COMPLETION FAILED:" + ex.getMessage() + " \n\n");
            }
        };

        Consumer<ServiceBusErrorContext> processError = error -> {
            System.out.println("Error: " + error.getException().getMessage());
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString(Credentials.serviceBusConnectionString)
            .processor()
            .queueName(Credentials.serviceBusQueue)
            .processMessage(processMessage)
            .processError(processError)
            .prefetchCount(80)
            .maxConcurrentCalls(1) // <---- it doesn't use .parallel+.runOn operators
            .maxAutoLockRenewDuration(Duration.ofSeconds(0))
            .disableAutoComplete()
            .buildProcessorClient();


        processorClient.start();

        TimeUnit.MINUTES.sleep(3 * 60);
        System.out.println("Stopping....");
        processorClient.stop();
    }
}
