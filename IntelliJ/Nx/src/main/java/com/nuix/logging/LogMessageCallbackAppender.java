package com.nuix.logging;

import lombok.NonNull;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.function.Consumer;
import java.util.function.Function;

/***
 * A custom log4j2 appender which forwards String rendered {@link LogEvent} objects to the provided consumer, using the
 * provded layout to first render the LogEvent.
 */
public class LogMessageCallbackAppender extends AbstractAppender {
    private Consumer<String> messageConsumer;

    /***
     * Creates a new instance with the given name and filter
     * @param name The name to assign
     * @param layout The required layout which will be used to render {@link LogEvent} instances into Strings
     * @param filter The filter to apply
     */
    public LogMessageCallbackAppender(String name, @NonNull Layout layout, Filter filter) {
        super(name, filter, layout, false, null);
    }

    /***
     * Creates a new instance with the given name and filter
     * @param name The name to assign
     * @param layout The required layout which will be used to render {@link LogEvent} instances into Strings
     * @param filteringFunction A function which will be used to determine what events are filtered.
     */
    public LogMessageCallbackAppender(String name, @NonNull Layout layout, Function<LogEvent,Boolean> filteringFunction) {
        super(name, null, layout, false, null);
        if(filteringFunction != null) {
            Filter filter = LogHelper.getInstance().createPassFailFilter(filteringFunction);
            addFilter(filter);
        }
    }

    /***
     * Creates a new instance with the given name and a filter that accepts ALL events
     * @param name The name to assign
     */
    public LogMessageCallbackAppender(String name, @NonNull Layout layout) {
        this(name,layout, (Filter) null);
        this.addFilter(LogHelper.getInstance().createAcceptAllFilter());
    }

    /***
     * {@inheritDoc}
     * @param event
     */
    @Override
    public void append(LogEvent event) {
        if(messageConsumer != null) {
            String message = new String(getLayout().toByteArray(event));
            messageConsumer.accept(message);
        }
    }

    /***
     * Gets the Consumer that will be provided log event messages (based on provided layout)
     * @return The Consumer that will receive log event messages
     */
    public Consumer<String> getMessageConsumer() {
        return messageConsumer;
    }

    /***
     * Sets the consumer that will be provided log event messages (based on provided layout)
     * @param messageConsumer The Consumer to receive log event messages
     */
    public void setMessageConsumer(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
}
