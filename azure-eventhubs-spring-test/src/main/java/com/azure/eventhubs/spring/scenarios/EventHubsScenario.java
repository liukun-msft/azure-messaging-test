package com.azure.eventhubs.spring.scenarios;


import com.azure.eventhubs.spring.util.CmdlineArgs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventHubsScenario {
    @Autowired
    protected CmdlineArgs cmdlineArgs;

    public abstract void run();
}
