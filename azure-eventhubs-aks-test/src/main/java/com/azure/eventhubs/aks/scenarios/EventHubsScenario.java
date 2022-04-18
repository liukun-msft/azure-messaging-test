package com.azure.eventhubs.aks.scenarios;


import com.azure.eventhubs.aks.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventHubsScenario {
    @Autowired
    protected ScenarioOptions options;

    public abstract void run();
}
