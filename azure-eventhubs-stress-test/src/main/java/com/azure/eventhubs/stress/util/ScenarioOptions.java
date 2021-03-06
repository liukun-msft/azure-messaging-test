package com.azure.eventhubs.stress.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ScenarioOptions {
    private final ApplicationArguments args;
    @Autowired
    private Environment env;

    @Autowired
    public ScenarioOptions(ApplicationArguments args) {
        this.args = args;
    }

    public String get(String name) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else {
            return env.getProperty(name);
        }
    }

    public String get(String name, String defaultValue) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else if (System.getenv().containsKey(name)) {
            return env.getProperty(name);
        } else {
            return defaultValue;
        }
    }
}