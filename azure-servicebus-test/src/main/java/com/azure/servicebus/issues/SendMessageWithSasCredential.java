package com.azure.servicebus.issues;


import com.azure.core.credential.AzureSasCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.util.Credentials;

import java.util.Arrays;
import java.util.List;


public class SendMessageWithSasCredential {

    public static void main(String[] args) {
        AzureSasCredential credential = new AzureSasCredential(Credentials.serviceBusSignature);

        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(Credentials.serviceBusNamespace, credential)
                .sender()
                .queueName(Credentials.serviceBusQueue)
                .buildClient();

        List<ServiceBusMessage> messages = Arrays.asList(
                new ServiceBusMessage("Hello world").setMessageId("1"),
                new ServiceBusMessage("Bonjour").setMessageId("2"));

        sender.sendMessages(messages);

        sender.close();
    }

}
