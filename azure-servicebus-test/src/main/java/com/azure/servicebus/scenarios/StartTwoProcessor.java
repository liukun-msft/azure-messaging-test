package com.azure.servicebus.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.util.Credentials;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service("StartTwoProcessor")
public class StartTwoProcessor extends ServiceBusScenario {

    @Override
    public void run() {
        String queue1 = cmdlineArgs.get("queue1");
        String queue2 = cmdlineArgs.get("queue2");

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println("message id:" + messageContext.getMessage().getMessageId());
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        ServiceBusClientBuilder busClientBuilder = new ServiceBusClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString);

        ServiceBusProcessorClient processorClient1 = busClientBuilder
                .processor()
                .queueName(queue1)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        ServiceBusProcessorClient processorClient2 = busClientBuilder
                .processor()
                .queueName(queue2)
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        processorClient1.start();
        processorClient2.start();
    }
}

