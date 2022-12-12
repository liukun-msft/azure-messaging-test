package com.azure.servicebus.spring.issues.error;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.servicebus.spring.scenarios.ServiceBusScenario;
import org.springframework.stereotype.Service;

@Service("SendTopicMessages")
public class SendTopicMessages extends ServiceBusScenario {

    private static final int SEND_MESSAGE_NUMBER = 100;

    @Override
    public void run() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(options.getServicebusConnectionString())
                .sender()
                .topicName(options.getServicebusTopicName())
                .buildClient();

        for (int i = 0; i < SEND_MESSAGE_NUMBER; i++) {
            ServiceBusMessage message = new ServiceBusMessage("Hello world").setMessageId("" + i);

            sender.sendMessage(message);

            System.out.println("Send Message id: " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sender.close();
    }
}
