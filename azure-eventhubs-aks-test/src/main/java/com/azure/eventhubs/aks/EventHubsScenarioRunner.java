package com.azure.eventhubs.aks;

import com.azure.eventhubs.aks.scenarios.EventHubsScenario;
import com.azure.eventhubs.aks.util.Constants;
import com.azure.eventhubs.aks.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class EventHubsScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ScenarioOptions options;

    public static void main(String[] args) {
        SpringApplication.run(EventHubsScenarioRunner.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        String scenarioName = options.get(Constants.TEST_CLASS);
        EventHubsScenario scenario = (EventHubsScenario) applicationContext.getBean(scenarioName);
        scenario.run();
    }
}