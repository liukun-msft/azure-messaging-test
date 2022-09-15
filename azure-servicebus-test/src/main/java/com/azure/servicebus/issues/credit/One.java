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

public class One {
    //  The One.java demonstrate the Processor getting hang with the Run-Setup
    //  where maxConcurrentCalls>1 && maxConcurrentCalls == defaultBoundedElasticSize.
    //
    //  Run-Setup
    //  =========
    //  |--------------------|----------------------------------------------------|
    //  |   Processor Setup: |  .prefetchCount(80)                                |
    //  |                    |  .maxConcurrentCalls(2)                            |
    //  |------------------- |----------------------------------------------------|
    //  |  JVM Option        |  -Dreactor.schedulers.defaultBoundedElasticSize=2  |
    //  |--------------------|----------------------------------------------------|
    //  |   SDK Code tweak   |  NONE                                              |
    //  |--------------------|----------------------------------------------------|
    //
    //
    //   Internal Message Flow
    //   =====================
    //
    //   Given below the Deliveries flow internal to SDK before it reaches the consumer.
    //   There are 4 points where the Deliveries are buffered, additionally Thread-Hoping
    //   happens in the last 3 points.
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
    //   Thread-Hoping
    //   -------------
    //      PublishOnA: Reactor-Executor         -> BoundedElasticThread_{i}
    //      PublishOnB: BoundedElasticThread_{i} -> BoundedElasticThread_{i}
    //      RunOn:      BoundedElasticThread_{i} -> BoundedElasticThread_{i}
    //
    //  When running One.java we don't change the above buffering or Thread-Hoping(s),
    //  we can observe the Processor getting hang.
    //
    //  One workaround our team identified to immediately unblock a customer
    //  hitting similar hang was to increase the defaultBoundedElasticSize.
    //
    //  See Two.java for a better/long-term solution.
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
