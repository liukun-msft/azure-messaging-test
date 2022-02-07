package com.azure.servicebus.util;

import org.springframework.core.env.Environment;

import java.util.Objects;


public class Credentials {
    public static String serviceBusConnectionString;
    public static String serviceBusQueue;
    public static String serviceBusNamespace;
    public static String serviceBusSignature;

    /*
      setCredential() will set credentials from system env first.
      If it is null, then set from system property.
    * */
    public static void setCredentials(Environment environment) {
        serviceBusConnectionString = Objects.requireNonNullElse(System.getenv(Constants.AZURE_SERVICE_BUS_CONNECTION_STRING), environment.getProperty(Constants.AZURE_SERVICE_BUS_CONNECTION_STRING));
        serviceBusQueue = Objects.requireNonNullElse(System.getenv(Constants.AZURE_SERVICE_BUS_QUEUE), environment.getProperty(Constants.AZURE_SERVICE_BUS_QUEUE));
        serviceBusNamespace = Objects.requireNonNullElse(System.getenv(Constants.AZURE_SERVICE_BUS_NAMESPACE), environment.getProperty(Constants.AZURE_SERVICE_BUS_NAMESPACE));
        serviceBusSignature = Objects.requireNonNullElse(System.getenv(Constants.AZURE_SERVICE_BUS_SIGNATURE), environment.getProperty(Constants.AZURE_SERVICE_BUS_SIGNATURE));
    }

}
