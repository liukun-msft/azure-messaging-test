package com.azure.eventhubs.stress.scenarios;


import com.azure.eventhubs.stress.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventHubsScenario {
    @Autowired
    protected ScenarioOptions options;

    public abstract void run();
}
