package com.azure.eventhubs;

import com.azure.eventhubs.scenarios.EventHubsScenario;
import com.azure.eventhubs.util.CmdlineArgs;
import com.azure.eventhubs.util.Constants;
import com.azure.eventhubs.util.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.Objects;


@SpringBootApplication
public class EventHubsScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected CmdlineArgs cmdlineArgs;

    @Autowired
    protected Environment environment;

    @PostConstruct
    public void setProperties(){
        Credentials.setCredentials(environment);
    }

    public static void main(String[] args) {
        SpringApplication.run(EventHubsScenarioRunner.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        String scenarioName = Objects.requireNonNull(cmdlineArgs.get(Constants.SCENARIO_OPTION), "scenario should be provide, for example: --scenario=SendSimpleMessage");
        EventHubsScenario scenario = (EventHubsScenario) applicationContext.getBean(scenarioName);
        scenario.run();
    }
}