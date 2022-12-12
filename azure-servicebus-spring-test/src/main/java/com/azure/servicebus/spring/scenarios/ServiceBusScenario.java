package com.azure.servicebus.spring.scenarios;


import com.azure.servicebus.spring.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ServiceBusScenario {
    @Autowired
    protected ScenarioOptions options;

    public abstract void run();
}
