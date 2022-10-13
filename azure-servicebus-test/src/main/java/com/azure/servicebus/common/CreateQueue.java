package com.azure.servicebus.common;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.servicebus.util.Credentials;

public class CreateQueue {

    public static void main(String[] args) {
        HttpLogOptions logOptions = new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.HEADERS);

        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
                .connectionString(Credentials.serviceBusConnectionString)
                .httpLogOptions(logOptions)
                .buildClient();

        String queueName = "stress-test-queue";

        for(int i = 1; i <= 20; i++){
            client.createQueue(queueName + i);
        }

    }
}
