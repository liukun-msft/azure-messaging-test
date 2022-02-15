package com.azure.eventhubs.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Objects;

import static com.azure.eventhubs.util.Constants.AZURE_EVENT_HUBS_CONNECTION_STRING;

public class Credentials {
    public static String eventHubsConnectionString;
    public static String eventHub;
    public static String storageConnectionString;
    public static String storageContainer;
    public static String proxyUserName;
    public static String proxyPassword;

    /*
      setCredential() will set credentials from system env first.
      If it is null, then set from system property.
    * */
    public static void setCredentials(Environment environment) {
        eventHubsConnectionString = Objects.requireNonNullElse(System.getenv(Constants.AZURE_EVENT_HUBS_CONNECTION_STRING), environment.getProperty(AZURE_EVENT_HUBS_CONNECTION_STRING));
        eventHub = Objects.requireNonNullElse(System.getenv(Constants.AZURE_EVENT_HUB), environment.getProperty(Constants.AZURE_EVENT_HUB));
        storageConnectionString = Objects.requireNonNullElse(System.getenv(Constants.AZURE_STORAGE_CONNECTION_STRING), environment.getProperty(Constants.AZURE_STORAGE_CONNECTION_STRING));
        storageContainer = Objects.requireNonNullElse(System.getenv(Constants.AZURE_STORAGE_CONTAINER), environment.getProperty(Constants.AZURE_STORAGE_CONTAINER));
        proxyUserName = Objects.requireNonNullElse(System.getenv(Constants.PROXY_USER_NAME), environment.getProperty(Constants.PROXY_USER_NAME));
        proxyPassword = Objects.requireNonNullElse(System.getenv(Constants.PROXY_PASSWORD), environment.getProperty(Constants.PROXY_PASSWORD));

    }

}
