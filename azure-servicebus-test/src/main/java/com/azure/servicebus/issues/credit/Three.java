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

public class Three {
    //  The Two.java (& One.java) uses Processor with maxConcurrentCalls=2.
    //  When maxConcurrentCalls>1, the SDK code make use of .parallel+.runOn
    //  operators.
    //
    //  The maxConcurrentCalls=1 is a special case, where SDK code doesn't
    //  use .parallel+.runOn operators for optimization purposes.
    //
    //  The current SDK flow for maxConcurrentCalls=1 is:
    //
    //   ReceiveLinkHandler (buffering@Sinks.Many ✓)
    //    |
    //    |-- ServiceBusReactorReceiver (buffering@PublishOnA ✓)
    //         |
    //         |-- ServiceBusReceiveLinkProcessor (buffering@PublishOnB ✓)
    //              |
    //              |-- Consumer
    //
    //
    //   There are two Thread-Hoping points -
    //
    //      PublishOnA: Reactor-Executor         -> BoundedElasticThread_{i}
    //      PublishOnB: BoundedElasticThread_{i} -> BoundedElasticThread_{i}
    //
    //  Clearly we can/should remove one Thread-Hoping. The Three.java disables one
    //  Thread-Hoping i.e. the new flow become:
    //
    //   ReceiveLinkHandler (buffering@Sinks.Many ✓)
    //    |
    //    |-- ServiceBusReactorReceiver (buffering@PublishOnA ✓)
    //         |
    //         |-- ServiceBusReceiveLinkProcessor (buffering@PublishOnB x)
    //              |
    //              |-- Consumer
    //
    //   For the prototyping purpose, the PublishOnB is disabled by setting an
    //   environment variable, when running Three.java make sure you have
    //   the following setup.
    //
    //  Run-Setup
    //  =========
    //  |--------------------|----------------------------------------------------|
    //  |   Processor Setup: |  .prefetchCount(80)                                |
    //  |                    |  .maxConcurrentCalls(1)                            |
    //  |------------------- |----------------------------------------------------|
    //  |  JVM Option        |  -Dreactor.schedulers.defaultBoundedElasticSize=2  |
    //  |--------------------|----------------------------------------------------|
    //  |   SDK Code tweak   |  One Thread-Hoping must be disabled via env-var    |
    //  |                    |                                                    |
    //  |                    |  PUB_ON_IN_SB_LINK_PROCESSOR_OFF=1                 |
    //  |--------------------|----------------------------------------------------|
    //
    //  When running Three.java, we can observe the Processor getting hang for maxConcurrentCalls=1.
    //  If we look at the outputs from SDK print statements, the reason for it
    //  getting stuck is -
    //
    //  1. ReceiveLinkHandler buffered 80 Deliveries.
    //  2. One Delivery-Message is pulled from the ReceiveLinkHandler buffer and
    //     handled over to the downstream (Consumer / Processor handler).
    //  3. The Processor handler requested completion (disposition) of the Delivery-Message.
    //  4. The Processor handler is blocked on complete API to return.
    //  5. The Delivery-Disposition-Ack for completion arrived and buffered at ReceiveLinkHandler.
    //  6. The Delivery-Disposition-Ack is not pulled from the buffer by
    //     the ReactorReceiver (ServiceBusReactorReceiver).
    //  7. The reason for not pulling is, there is no back pressure from the downstream. Without
    //     pulling the Delivery-Disposition-Ack, we cannot terminate the complete API.
    //  8. The backpressure from downstream happens only after processor handler returns, which is
    //     blocked on completion API.
    //  9. So the setup under Three.java runs is in a deadlock situation.
    //
    //  Learnings:
    //  ==========
    //      1. In the Processor context, we have identified one point where
    //         unnecessary Thread-Hoping happens when maxConcurrentCalls=1, and should be removed.
    //      2. We also observed that Processor is entering a deadlock.

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
