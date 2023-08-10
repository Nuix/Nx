package com.nuix.logging;

import lombok.NonNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;


/***
 * Provides various convenience methods for log4j2 related tasks, since doing some operations programmatically in log4j2
 * are not very straightforward.
 */
public class LogHelper {
    private Logger logger;

    private static LogHelper instance;
    public static synchronized LogHelper getInstance() {
        if(instance == null) { instance = new LogHelper(); }
        return instance;
    }
    private LogHelper(){
        logger = getLogger();
    }

    /***
     * Gets the current level of the root logger
     * @return The root logger's current level
     */
    public Level getRootLoggerLevel() {
        LoggerContext lc = (LoggerContext) LogManager.getContext(false);
        return lc.getRootLogger().getLevel();
    }

    /***
     * Sets the root logger's level, returning the level it was set to before changing it, in case you wish to later
     * set it back to where it was.
     * @param level The new level to assign to the root logger
     * @return The root logger's level before changing it
     */
    public Level setRootLoggerLevel(Level level) {
        // https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2
        Level priorLevel = getRootLoggerLevel();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
        return priorLevel;
    }

    public Level setRootLoggerLevelOff() { return setRootLoggerLevel(Level.OFF); }
    public Level setRootLoggerLevelFatal() { return setRootLoggerLevel(Level.FATAL); }
    public Level setRootLoggerLevelError() { return setRootLoggerLevel(Level.ERROR); }
    public Level setRootLoggerLevelWarn() { return setRootLoggerLevel(Level.WARN); }
    public Level setRootLoggerLevelInfo() { return setRootLoggerLevel(Level.INFO); }
    public Level setRootLoggerLevelDebug() { return setRootLoggerLevel(Level.DEBUG); }
    public Level setRootLoggerLevelTrace() { return setRootLoggerLevel(Level.TRACE); }
    public Level setRootLoggerLevelAll() { return setRootLoggerLevel(Level.ALL); }

    /***
     * Creates a file appender which will log to the specified file.  If a pattern is provided, it
     * will be used as the layout
     * (see <a href="https://logging.apache.org/log4j/2.x/manual/layouts.html#Pattern_Layout">docs</a>).
     * If no pattern is supplied, then a default of "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n" will be used.
     * If a filter is provided, it will be used to determine which log events are ultimately written to the created
     * file appender (and therefore the log file this creates).  If no filter is provided, a default one will be used
     * which accepts all log events it is provided.
     * @param logFile The absolute file path to which this appender will write events
     * @param pattern The logging pattern, uses "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n" if null provided.
     * @param filter The filter used to determine which log events are written.  Will accept all events if null is provided.
     * @return The FileAppender object, which could be used to later remove/close the appender.
     */
    public FileAppender initAlternateLogFile(@NonNull File logFile, String pattern, Filter filter){
        // If we are not provided a filter then we will supply a default "accept all" filter
        if(filter == null){
            filter = createAcceptAllFilter();
        }

        // If we are not provided a pattern, use a default Nuix style one
        if(pattern == null || pattern.isBlank()){
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n";
        }

        // Attempt to name appender after caller class
        String name = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().getCanonicalName();
        name += "_" + System.currentTimeMillis();

        LoggerContext lc = (LoggerContext) LogManager.getContext(false);
        FileAppender fa = FileAppender.newBuilder().setName(name).withAppend(true)
                .setFilter(filter)
                .withFileName(logFile.getAbsolutePath())
                .setLayout(PatternLayout.newBuilder().withPattern(pattern).build())
                .setConfiguration(lc.getConfiguration())
                .build();

        attachLogAppender(fa);

        return fa;
    }

    /***
     * Attaches a new {@link ConsoleAppender}
     * @param pattern The pattern to use, a null value will default to <code>%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n</code>
     * @param filter The filter to use, a null value will default to filter returned by {@link #createAcceptAllFilter()}.
     * @return Returns the newly created and attached console appender
     */
    public ConsoleAppender attachConsoleAppender(String pattern, Filter filter) {
        // If we are not provided a filter then we will supply a default "accept all" filter
        if(filter == null){
            filter = createAcceptAllFilter();
        }

        // If we are not provided a pattern, use a default Nuix style one
        if(pattern == null || pattern.isBlank()){
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n";
        }

        // Attempt to name appender after caller class
        String name = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().getCanonicalName();
        name += "_" + System.currentTimeMillis();

        LoggerContext lc = (LoggerContext) LogManager.getContext(false);

        ConsoleAppender ca = ConsoleAppender.newBuilder()
                .setName(name)
                .setFilter(filter)
                .setLayout(PatternLayout.newBuilder().withPattern(pattern).build())
                .setConfiguration(lc.getConfiguration()).build();

        attachLogAppender(ca);

        return ca;
    }

    /***
     * Attaches the given appender
     * @param appender The appender to attach
     */
    public void attachLogAppender(@NonNull Appender appender) {
        LoggerContext lc = (LoggerContext) LogManager.getContext(false);
        appender.start();
        lc.getConfiguration().addAppender(appender);
        lc.getRootLogger().addAppender(lc.getConfiguration().getAppender(appender.getName()));
        lc.updateLoggers();
    }

    /***
     * Removes the given appender
     * @param appender The appender to remove
     */
    public void removeAppender(@NonNull Appender appender) {
        appender.stop();
        LoggerContext lc = (LoggerContext) LogManager.getContext(false);
        lc.getRootLogger().removeAppender(lc.getConfiguration().getAppender(appender.getName()));
        lc.updateLoggers();
    }

    /***
     * Attaches a {@link ConsoleAppender}
     * @param pattern The pattern to use
     * @param filteringFunction A function that will receive each log event, returning True to ACCEPT and False to DENY.
     * @return The newly created console appender
     */
    public ConsoleAppender attachConsoleAppender(String pattern, Function<LogEvent,Boolean> filteringFunction) {
        Filter filter = createPassFailFilter(filteringFunction);
        return attachConsoleAppender(pattern, filter);
    }

    /***
     * Creates a file appender which will log to the specified file.  If a pattern is provided, it
     * will be used as the layout
     * (see <a href="https://logging.apache.org/log4j/2.x/manual/layouts.html#Pattern_Layout">docs</a>).
     * If no pattern is supplied, then a default of "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n" will be used.
     * If a filter is provided, it will be used to determine which log events are ultimately written to the created
     * file appender (and therefore the log file this creates).  If no filter is provided, a default one will be used
     * which accepts all log events it is provided.
     *
     * This differs from the method {@link #initAlternateLogFile(File, String, Filter)} in that this function accepts
     * a generic Function&lt;LogEvent,Boolean&gt; which will be wrapped by an anonymous {@link AbstractFilter} instance.
     * When the provided filtering function returns True, the wrapped filter will return an ACCEPT result.  When it
     * returns False, the wrapped will return a DENY result.  This method exists mostly to make working with it from
     * a scripting language (such as Ruby) potentially easier.  Example in Ruby:<br>
     *
     * <pre>
     * {@code
     * LogHelper.initAlternateLogFile(java.io.File.new("D:\\temp\\script.log"),nil) do |event|
     * 	next (!event.nil? && event.getLoggerName.startsWith("proservTest"))
     * end
     * }
     * </pre>
     *
     * @param logFile The absolute file path to which this appender will write events
     * @param pattern The logging pattern, uses "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n" if null provided.
     * @param filteringFunction A function that will receive each log event, returning True to ACCEPT and False to DENY.
     */
    public void initAlternateLogFile(@NonNull File logFile, String pattern, Function<LogEvent,Boolean> filteringFunction){
        Filter filter = createPassFailFilter(filteringFunction);
        initAlternateLogFile(logFile, pattern, filter);
    }

    /***
     * Attaches to logging system and returns a {@link LogMessageCallbackAppender} whose purpose is to forward rendered
     * log event strings to a {@link Consumer} which then in turn makes use of that message.
     * For example, if you wish to forward log messages to the Nuix script console you might do something like:
     * <pre>{@code
     * pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n"
     * appender = LogHelper.getInstance.attachLogMessageCallbackAppender(pattern){|message| puts message}
     * }</pre>
     * Note that by default, no filtering is applied to log events!  If you wish to control which log events get forwarded
     * the you will want to instead do something like this:
     * <pre>{@code
     * pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n"
     * appender = LogHelper.getInstance.attachLogMessageCallbackAppender(pattern){|message| puts message}
     * filter = LogHelper.getInstance.createPassFailFilter do |log_event|
     *  return log_event.getLoggerName == "MY_SPECIAL_LOGGER"
     * end
     * appender.addFilter(filter)
     * }</pre>
     * @param pattern The pattern used to construct the {@link PatternLayout} that will be used to render {@link LogEvent}
     *                instances into strings.
     * @param consumer The consumer which will receive log messages.
     * @return The appender so you can remove it at a later time using {@link #removeAppender(Appender)}.
     */
    public LogMessageCallbackAppender attachLogMessageCallbackAppender(String pattern, Consumer<String> consumer) {
        String name = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().getCanonicalName();
        name += "_" + System.currentTimeMillis();

        // If we are not provided a pattern, use a default Nuix style one
        if(pattern == null || pattern.isBlank()){
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n";
        }

        PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();

        LogMessageCallbackAppender appender = new LogMessageCallbackAppender(name, layout);
        appender.setMessageConsumer(consumer);
        attachLogAppender(appender);
        return appender;
    }

    /***
     * Attaches to logging system and returns a {@link LogMessageCallbackAppender} whose purpose is to forward rendered
     * log event strings to a {@link Consumer} which then in turn makes use of that message.
     * This will also attach a {@link Filter} which will accept any LogEvents in which the logger name matches one of the
     * provided regex patterns.
     * For example, if you wish to forward log messages to the Nuix script console you might do something like:
     * <pre>{@code
     * pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n"
     * regex_patterns = ["com\.nuix\.proserv.*"] # Forward events from this library
     * appender = LogHelper.getInstance.attachLogMessageCallbackAppender(pattern){|message| puts message}
     * }</pre>
     * @param pattern The pattern used to construct the {@link PatternLayout} that will be used to render {@link LogEvent}
     *                instances into strings.
     * @param regexPatterns One or more regular expressions that will be used by the filter to filter log events by their
     *                      logger name.
     * @param consumer The consumer which will receive log messages.
     * @return The appender so you can remove it at a later time using {@link #removeAppender(Appender)}.
     */
    public LogMessageCallbackAppender attachLogMessageCallbackAppender(String pattern, Collection<String> regexPatterns,
                                                                       Consumer<String> consumer) {
        String name = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().getCanonicalName();
        name += "_" + System.currentTimeMillis();

        // If we are not provided a pattern, use a default Nuix style one
        if(pattern == null || pattern.isBlank()){
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n";
        }
        PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();

        final List<Pattern> compiledPatterns = new ArrayList<>();
        for(String regexPattern : regexPatterns) {
            try {
                compiledPatterns.add(Pattern.compile(regexPattern));
            } catch (Exception exc) {
                logger.error("attachLogMessageCallbackAppender, invalid regex pattern: "+regexPattern);
            }
        }

        Function<LogEvent, Boolean> filterFunction = new Function<LogEvent, Boolean>() {
            private Set<String> knownPassingNames = new HashSet<>();
            @Override
            public Boolean apply(LogEvent logEvent) {
                String loggerName = logEvent.getLoggerName();

                // Have we already previously determined that this name matches?  If so
                // we can skip having to run regex test.
                if(knownPassingNames.contains(loggerName)) {
                    return true;
                } else {
                    for(Pattern compiledPattern : compiledPatterns) {
                        if(compiledPattern.matcher(loggerName).matches()){
                            // Store that this was a match so we don't need to regex match again
                            knownPassingNames.add(loggerName);
                            return true;
                        }
                    }
                }

                return false;
            }
        };

        LogMessageCallbackAppender appender = new LogMessageCallbackAppender(name, layout, createPassFailFilter(filterFunction));
        appender.setMessageConsumer(consumer);
        attachLogAppender(appender);

        return appender;
    }

    /***
     * Convenience method for obtaining a logger object.
     * @param name The name to assign the logger
     * @return A logger object
     */
    public Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }

    /***
     * Convenience method for obtaining a logger object, named after the class calling this method.  Internally
     * the class name is determined and then used in call to {@link #getLogger(String)}.
     * @return A logger object
     */
    public Logger getLogger() {
        String name = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass().getCanonicalName();
        return getLogger(name);
    }

    /***
     * Convenience method for converting a Function&lt;LogEvent,Boolean&gt; into a {@link AbstractFilter} which
     * will yield ACCEPT when the function returns True and DENY when the function returns False.
     * @param filteringFunction A function which will be used to determine what events are filtered.
     * @return A {@link Filter} instance
     */
    public Filter createPassFailFilter(Function<LogEvent,Boolean> filteringFunction) {
        return new AbstractFilter() {
            @Override
            public Result filter(LogEvent event) {
                boolean result = filteringFunction.apply(event);
                return (result == true ? Result.ACCEPT : Result.DENY);
            }
        };
    }

    /***
     * Creates a Filter which accepts all log events.
     * @return A Filter which accepts all log events blindly
     */
    public Filter createAcceptAllFilter() {
        return new AbstractFilter() {
            @Override
            public Result filter(LogEvent event) {
                return Result.ACCEPT;
            }
        };
    }
}
