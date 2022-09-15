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

public class Two {
    //   The One.java demonstrated Processor getting stuck when running
    //   under the setup maxConcurrentCalls >  && maxConcurrentCalls == defaultBoundedElasticSize.
    //
    //   The One.java also described the current buffering and Thread-Hoping points.
    //
    //   ReceiveLinkHandler (buffering1@Sinks.Many ✓)
    //    |
    //    |-- ServiceBusReactorReceiver (buffering2@PublishOnA ✓)
    //         |
    //         |-- ServiceBusReceiveLinkProcessor (buffering3@PublishOnB ✓)
    //              |
    //              |-- ServiceBusProcessorClient (buffering4@Parallel+RunOn ✓)
    //                  |
    //                  |-- Consumer
    //
    //   One straight forward observation is, the Thread-Hoping at PublishOnA
    //   and PublishOnB is unnecessary, the Thread-Hoping at RunOn is sufficient
    //   to meet the maxConcurrentCalls requirement for the processor.
    //
    //   The Two.java disables these two Thread-Hoping i.e. the new flow become:
    //
    //   ReceiveLinkHandler (buffering1@Sinks.Many ✓)
    //    |
    //    |-- ServiceBusReactorReceiver (buffering2@PublishOnA x)
    //         |
    //         |-- ServiceBusReceiveLinkProcessor (buffering3@PublishOnB x)
    //              |
    //              |-- ServiceBusProcessorClient (buffering4@Parallel+RunOn ✓)
    //                  |
    //                  |-- Consumer
    //
    //
    //   In Two.java, for prototyping purpose, the PublishOnA and PublishOnB can be disabled
    //   by setting two environment variables, when running Two.java make sure you have
    //   the following setup.
    //
    //  Run-Setup
    //  =========
    //  |--------------------|----------------------------------------------------|
    //  |   Processor Setup: |  .prefetchCount(80)                                |
    //  |                    |  .maxConcurrentCalls(2)                            |
    //  |------------------- |----------------------------------------------------|
    //  |  JVM Option        |  -Dreactor.schedulers.defaultBoundedElasticSize=2  |
    //  |--------------------|----------------------------------------------------|
    //  |   SDK Code tweak   |  Two Thread-Hoping must be disabled via env-vars   |
    //  |                    |                                                    |
    //  |                    |  PUB_ON_IN_SB_LINK_PROCESSOR_OFF=1                 |
    //  |                    |  PUB_ON_IN_SB_REACTOR_RECVR_OFF=1                  |
    //  |--------------------|----------------------------------------------------|
    //
    //  Learnings:
    //  ==========
    //      1. In the Processor context, we have identified two points where unnecessary
    //         Thread-Hoping happens when maxConcurrentCalls>1, and should be removed.
    //      2. As a result of doing #1, we save allocation & cpu but also
    //         the Processor no longer get stuck.
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
            .maxConcurrentCalls(2)
            .maxAutoLockRenewDuration(Duration.ofSeconds(0))
            .disableAutoComplete()
            .buildProcessorClient();


        processorClient.start();

        TimeUnit.MINUTES.sleep(3 * 60);
        System.out.println("Stopping....");
        processorClient.stop();
    }
}
