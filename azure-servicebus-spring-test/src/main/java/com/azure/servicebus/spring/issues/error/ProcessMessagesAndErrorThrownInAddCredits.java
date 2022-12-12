package com.azure.servicebus.spring.issues.error;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.servicebus.spring.scenarios.ServiceBusScenario;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Component("ProcessMessagesAndErrorThrownInAddCredits")
public class ProcessMessagesAndErrorThrownInAddCredits extends ServiceBusScenario {

    @Override
    public void run() {

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println(messageContext.getMessage().getMessageId());
                // other message processing code
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
            }
        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(options.getServicebusConnectionString())
                .processor()
                .maxConcurrentCalls(10)
                .topicName(options.getServicebusTopicName())
                .subscriptionName(options.getServicebusSubscriptionName())
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient();

        processorClient.start();


    }
}
