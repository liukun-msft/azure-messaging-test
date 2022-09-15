// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.servicebus.issues.credit;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Six {
    //
    //  Six.java puts all together and demonstrate that maxConcurrentCalls>1 continue to work
    //  with all tweaking made so far.
    //
    //  Run-Setup
    //  =========
    //  |--------------------|----------------------------------------------------|
    //  |   Processor Setup: |  .prefetchCount(80)                                |
    //  |                    |  .maxConcurrentCalls(2)                            |
    //  |------------------- |----------------------------------------------------|
    //  |  JVM Option        |  -Dreactor.schedulers.defaultBoundedElasticSize=2  |
    //  |--------------------|----------------------------------------------------|
    //  |   SDK Code tweak   |                                                    |
    //  |                    |                                                    |
    //  |                    |  PUB_ON_IN_SB_LINK_PROCESSOR_OFF=1                 |
    //  |                    |  PUB_ON_IN_SB_REACTOR_RECVR_OFF=1                  |
    //  |                    |  STREAM_DISPOSITION_ACKS=1                         |
    //  |                    |  HANDLER_Q_SIZE_FOR_CREDIT=1                       |
    //  |--------------------|----------------------------------------------------|
    //
    //   ReceiveLinkHandler (buffering1a@Sinks.Many for Delivery-Message,buffering1b@Sinks.Many for Delivery-Disposition-Ack ✓)
    //    |     \                           \
    //    |      \ Delivery-Message(s)       \----- Delivery-Disposition-Ack(s)
    //    |       \
    //    |-- ServiceBusReactorReceiver (buffering2@PublishOn x)
    //         |    \
    //         |     \ Delivery-Message(s)
    //         |      \
    //         |-- ServiceBusReceiveLinkProcessor (buffering3@PublishOn x)  [Credit calculation includes size of buffering1a]
    //              |   \
    //              |    \ Delivery-Message(s)
    //              |     \
    //              |-- ServiceBusProcessorClient (buffering4@Parallel+RunOn ✓)
    //                  |   \
    //                  |    \ Delivery-Message(s)
    //                  |     \
    //                  |-- Consumer
    //
    public static void main(String[] args) throws InterruptedException {

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                // Sleep 1 second to mock process progress
                TimeUnit.SECONDS.sleep(600);
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
            .prefetchCount(20)
            .maxConcurrentCalls(10)
            .maxAutoLockRenewDuration(Duration.ofSeconds(0))
            .disableAutoComplete()
            .buildProcessorClient();


        processorClient.start();

        TimeUnit.MINUTES.sleep(3 * 60);
        System.out.println("Stopping....");
        processorClient.stop();
    }
}
