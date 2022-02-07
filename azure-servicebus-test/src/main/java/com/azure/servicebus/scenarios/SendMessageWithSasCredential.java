package com.azure.servicebus.scenarios;


import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import com.azure.servicebus.util.Credentials;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service("SendMessageWithSasCredential")
public class SendMessageWithSasCredential extends ServiceBusScenario{

    @Override
    public void run() {
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
