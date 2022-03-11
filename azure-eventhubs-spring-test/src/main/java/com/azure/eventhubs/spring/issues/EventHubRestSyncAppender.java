package com.azure.eventhubs.spring.issues;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(name = "EventHubRestSyncAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class EventHubRestSyncAppender extends AbstractAppender {

    private final Layout<? extends Serializable> layout;
    private EventHubProducerAsyncClient evhProducer;

    private EventHubRestSyncAppender(String name, Layout<? extends Serializable> layout, Filter filter) {
        super(name, filter, null);

        this.layout = layout;

        final String connectionString = System.getenv("AZURE_EVENT_HUBS_CONNECTION_STRING");
        final String eventHub = System.getenv("AZURE_EVENT_HUB");

        try {
            this.evhProducer = new EventHubClientBuilder()
                    .connectionString(connectionString, eventHub)
                    .buildAsyncProducerClient();
        } catch (Exception e) {
            System.out.print(e.toString());
        }

    }

    @PluginFactory
    public static EventHubRestSyncAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout layout,
            @PluginElement("Filter") final Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new EventHubRestSyncAppender(name, layout, filter);
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(EventHubRestSyncAppender.class);

        ExecutorService pool = Executors.newFixedThreadPool(5);

        //send 100 messages on 5 thread
        for (int i = 0; i < 100; i++) {
            pool.execute(() -> logger.info("Test send info for log4j appender"));
        }
    }

    public void append(LogEvent event) {
        if (null != evhProducer) {
            byte[] payloadBytes = layout.toByteArray(event);
            evhProducer.createBatch().flatMap(batch -> {
                batch.tryAdd(new EventData(payloadBytes));
                return evhProducer.send(batch);
            }).subscribe(unused -> {
                    },
                    error -> System.err.println("An error occurred while sending logs to azure event hub : " + error),
                    () -> System.out.println("Azure event hub : send complete ! "));
        }
    }

    @Override
    public void stop() {
        evhProducer.close();
    }

}