package com.azure.eventhubs.scenarios;


import com.azure.eventhubs.util.CmdlineArgs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventHubsScenario {
    @Autowired
    protected CmdlineArgs cmdlineArgs;

    public abstract void run();
}
