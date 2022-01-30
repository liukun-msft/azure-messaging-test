package com.azure.servicebus.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CmdlineArgs {
    private final ApplicationArguments innerArgs;

    @Autowired
    public CmdlineArgs(ApplicationArguments innerArgs) {
        this.innerArgs = innerArgs;
    }

    public String get(String name) {
        if (innerArgs.containsOption(name)) {
            return innerArgs.getOptionValues(name).get(0);
        }
        return null;
    }
}