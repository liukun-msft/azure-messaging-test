package com.azure.servicebus;

import com.azure.servicebus.scenarios.ServiceBusScenario;
import com.azure.servicebus.util.CmdlineArgs;
import com.azure.servicebus.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Objects;


@SpringBootApplication
public class ServiceBusScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected CmdlineArgs cmdlineArgs;

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusScenarioRunner.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String scenarioName = Objects.requireNonNull(cmdlineArgs.get(Constants.SCENARIO_OPTION), "scenario should be provide, for example: --scenario=SendSimpleMessage");

        String className = Constants.SCENARIO_PATH + scenarioName;
        ServiceBusScenario scenario = (ServiceBusScenario) applicationContext.getBean(Class.forName(className));
        scenario.run();
    }
}