package com.azure.servicebus.scenarios;


import com.azure.servicebus.util.CmdlineArgs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ServiceBusScenario {

    @Autowired
    protected CmdlineArgs cmdlineArgs;

    public abstract void run();
}
