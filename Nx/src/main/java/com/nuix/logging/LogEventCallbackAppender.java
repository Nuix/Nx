package com.nuix.logging;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.function.Consumer;
import java.util.function.Function;

/***
 * A custom log4j2 appender which forwards {@link LogEvent} objects to the provided consumer
 */
public class LogEventCallbackAppender extends AbstractAppender {

    private Consumer<LogEvent> eventConsumer;

    /***
     * Creates a new instance with the given name and filter
     * @param name The name to assign
     * @param filter The filter to apply
     */
    public LogEventCallbackAppender(String name, Filter filter) {
        super(name, filter, null, false, null);
    }

    /***
     * Creates a new instance with the given name and filter
     * @param name The name to assign
     * @param filteringFunction A function which will be used to determine what events are filtered.
     */
    public LogEventCallbackAppender(String name, Function<LogEvent,Boolean> filteringFunction) {
        super(name, null, null, false, null);
        this.addFilter(LogHelper.getInstance().createPassFailFilter(filteringFunction));
    }

    /***
     * Creates a new instance with the given name and a filter that accepts ALL events
     * @param name The name to assign
     */
    public LogEventCallbackAppender(String name) {
        this(name, (Filter) null);
        this.addFilter(LogHelper.getInstance().createAcceptAllFilter());
    }

    /***
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void append(LogEvent event) {
        if(eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }

    /***
     * Gets the Consumer that will be provided log events
     * @return The Consumer that will receive log events
     */
    public Consumer<LogEvent> getEventConsumer() {
        return eventConsumer;
    }

    /***
     * Sets the consumer that will be provided log events
     * @param eventConsumer The Consumer to receive log events
     */
    public void setEventConsumer(Consumer<LogEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }
}
