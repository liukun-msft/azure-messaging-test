package com.azure.servicebus.spring;

import com.azure.servicebus.spring.scenarios.ServiceBusScenario;
import com.azure.servicebus.spring.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Objects;


@SpringBootApplication
public class ServiceBusScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ScenarioOptions options;

    @Autowired
    protected Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusScenarioRunner.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        String className = Objects.requireNonNull(options.getTestClass(), "The test class should be provided, please add --TEST_CLASS=<your test class> as start argument\"");
        ServiceBusScenario scenario = null;
        scenario = (ServiceBusScenario) applicationContext.getBean(className);
        scenario.run();
    }
}