package com.azure.servicebus.spring.scenarios;


import com.azure.servicebus.spring.util.CmdlineArgs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ServiceBusScenario {

    @Autowired
    protected CmdlineArgs cmdlineArgs;

    public abstract void run();
}
