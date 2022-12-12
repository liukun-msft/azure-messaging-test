// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.servicebus.spring.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScenarioOptions {
    @Value("${TEST_CLASS:#{null}}")
    private String testClass;

    @Value("${SERVICEBUS_CONNECTION_STRING:#{null}}")
    private String servicebusConnectionString;

    @Value("${SERVICEBUS_QUEUE_NAME:#{null}}")
    private String servicebusQueueName;

    @Value("${SERVICEBUS_TOPIC_NAME:#{null}}")
    private String servicebusTopicName;

    @Value("${SERVICEBUS_SUBSCRIPTION_NAME:#{null}}")
    private String servicebusSubscriptionName;


    public String getTestClass() {
        return testClass;
    }

    public String getServicebusConnectionString() {
        return servicebusConnectionString;
    }

    public String getServicebusQueueName() {
        return servicebusQueueName;
    }

    public String getServicebusTopicName() {
        return servicebusTopicName;
    }

    public String getServicebusSubscriptionName() {
        return servicebusSubscriptionName;
    }

}
