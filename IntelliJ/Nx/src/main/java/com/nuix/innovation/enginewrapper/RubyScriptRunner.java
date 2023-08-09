package com.nuix.innovation.enginewrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;

/***
 * Provides an easy way to execute Ruby scripts.
 */
public class RubyScriptRunner {
    private static final Logger log = LogManager.getLogger(RubyScriptRunner.class);

    /***
     * Writer implementation which specializes in forwarding to to consumer.  Used to forward
     * standard out and err to consumer from script container.
     */
    static class EventedWriter extends Writer {
        private Consumer<String> consumer;

        public EventedWriter(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
            synchronized (lock) {
                if (consumer != null) {
                    char[] subchars = new char[len];
                    System.arraycopy(cbuf, off, subchars, 0, len);
                    String value = new String(subchars);
                    SwingUtilities.invokeLater(() -> {
                        consumer.accept(value);
                    });
                }
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            consumer = null;
        }
    }

    protected ScriptingContainer scriptingContainer;
    protected Thread scriptThread;
    protected Consumer<String> standardOutput;
    protected Consumer<String> errorOutput;

    public RubyScriptRunner() {
    }

    /***
     * Interrupts running script thread if there is one running.  See also {@link #isAlive()}.
     */
    public void interrupt() {
        if (scriptThread != null) {
            scriptThread.interrupt();
        }
    }

    /***
     * Checks if script thread exists and is currently runnning.
     * @return True if script thread exists (non-null) and {@link Thread#isAlive()} returns true.
     */
    public boolean isAlive() {
        if (scriptThread != null) {
            return scriptThread.isAlive();
        } else {
            return false;
        }
    }

    /***
     * Joins script thread (if it exists) via {@link Thread#join(long)}.
     * @param timeoutMillis the time to wait in milliseconds
     * @throws InterruptedException if any thread has interrupted the current thread. The interrupted status of the
     * current thread is cleared when this exception is thrown.
     */
    public void join(long timeoutMillis) throws InterruptedException {
        if (scriptThread != null) {
            scriptThread.join(timeoutMillis);
        }
    }

    /***
     * Joins script thread (if it exists) via {@link Thread#join()}.
     * @throws InterruptedException if any thread has interrupted the current thread. The interrupted status of the
     * current thread is cleared when this exception is thrown.
     */
    public void join() throws InterruptedException {
        if (scriptThread != null) {
            scriptThread.join();
        }
    }

    /***
     * Sets the {@link Consumer} which will receive standard output while script is running.  If null when a script is
     * executed, received messages will be logged by this instance via log4j2.
     * @param standardOutput The consumer of running script's standard output
     */
    public void setStandardOutputConsumer(Consumer<String> standardOutput) {
        this.standardOutput = standardOutput;
    }

    /***
     * Sets the {@link Consumer} which will receive error output while script is running.  If null when a script is
     * executed, received messages will be logged by this instance via log4j2.
     * @param errorOutput
     */
    public void setErrorOutputConsumer(Consumer<String> errorOutput) {
        this.errorOutput = errorOutput;
    }

    /***
     * Runs a ruby script asynchronously
     * @param script A string containing the Ruby script
     * @param nuixVersion A string containing the Nuix version to be assigned to constant 'NUIX_VERSION'
     * @param variables A map of variables to inject into the script container.  Key is variable name, value is the
     *                  value to assign to that variable.  Prefix name with $ for global variables.
     */
    public void runScriptAsync(String script, String nuixVersion, Map<String, Object> variables) {
        initialize(nuixVersion, variables);

        scriptThread = new Thread(() -> {
            scriptingContainer.runScriptlet(script);
        });

        scriptThread.start();
    }

    /***
     * Runs a ruby script asynchronously
     * @param scriptFile A file containing a Ruby script
     * @param nuixVersion A string containing the Nuix version to be assigned to constant 'NUIX_VERSION'
     * @param variables A map of variables to inject into the script container.  Key is variable name, value is the
     *                  value to assign to that variable.  Prefix name with $ for global variables.
     */
    public void runFileAsync(File scriptFile, String nuixVersion, Map<String, Object> variables) {
        initialize(nuixVersion, variables);

        scriptThread = new Thread(() -> {
            try (InputStream scriptFileInputStream = FileUtils.openInputStream(scriptFile)) {
                scriptingContainer.runScriptlet(scriptFileInputStream, scriptFile.getAbsolutePath());
            } catch (Exception exc) {
                errorOutput.accept(ExceptionUtils.getMessage(exc) + "\n" + ExceptionUtils.getStackTrace(exc));
            }
        });

        scriptThread.start();
    }

    private void initialize(String nuixVersion, Map<String, Object> variablesToSet) {
        // Wait on any already running scripts
        try {
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (standardOutput == null) {
            standardOutput = log::info;
        }

        if (errorOutput == null) {
            errorOutput = log::error;
        }

        scriptingContainer = new ScriptingContainer(LocalVariableBehavior.TRANSIENT);

        scriptingContainer.setWriter(new EventedWriter(this.standardOutput));
        scriptingContainer.setErrorWriter(new EventedWriter(this.standardOutput));

        scriptingContainer.runScriptlet("NUIX_VERSION = \"" + nuixVersion + "\"");

        scriptingContainer.clear();
        for (Map.Entry<String, Object> variableToSet : variablesToSet.entrySet()) {
            log.info("Setting variable: '{}' to '{}'", variableToSet.getKey(), variableToSet.getValue());
            scriptingContainer.put(variableToSet.getKey(), variableToSet.getValue());
        }
    }
}
