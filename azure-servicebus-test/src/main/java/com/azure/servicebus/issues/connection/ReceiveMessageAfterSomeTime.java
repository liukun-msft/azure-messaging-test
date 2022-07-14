package com.azure.servicebus.issues.connection;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.servicebus.util.Credentials;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReceiveMessageAfterSomeTime {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countdownLatch = new CountDownLatch(1);
        AmqpRetryOptions options = new AmqpRetryOptions();
        options.setTryTimeout(Duration.ofSeconds(5));

        final ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .retryOptions(options)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .topicName(Credentials.serviceBusTopic)
                .subscriptionName(Credentials.serviceBusSubscription)
                .buildAsyncClient();

//        Flux.using(() -> true,
//                        client -> receiver.receiveMessages(),
//                        client -> System.err.println("failure"))
//                .retryWhen(
//                        Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
//                                .filter(throwable -> {
//                                    System.err.println("Current LowLevelClient's retry exhausted or a non-retryable error occurred." +
//                                            throwable);
//                                    return true;
//                                }))
        Disposable disposable = receiver.receiveMessages()
                .onErrorResume(error -> {
                    System.out.println("Encounter error when receive message: " + error);
                    return receiver.receiveMessages();
                })
                .subscribe(message -> {
                            System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
                                    message.getBody());
                            receiver.complete(message).block();
                        });

        countdownLatch.await(1, TimeUnit.HOURS);

        disposable.dispose();
        receiver.close();
    }

}
