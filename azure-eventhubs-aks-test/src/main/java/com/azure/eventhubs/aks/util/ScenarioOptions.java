package com.azure.eventhubs.aks.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScenarioOptions {
    private final ApplicationArguments args;

    @Autowired
    public ScenarioOptions(ApplicationArguments args) {
        this.args = args;
    }

    public String get(String name) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else {
            return System.getenv(name);
        }
    }

    public String get(String name, String defaultValue) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else if (System.getenv().containsKey(name)) {
            return System.getenv(name);
        } else {
            return defaultValue;
        }
    }
}