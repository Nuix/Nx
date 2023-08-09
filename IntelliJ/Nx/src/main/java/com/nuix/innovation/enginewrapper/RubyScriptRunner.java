package com.nuix.innovation.enginewrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

public class RubyScriptRunner {
    private static final Logger log = LogManager.getLogger(RubyScriptRunner.class);

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

    public void interrupt() {
        if (scriptThread != null) {
            scriptThread.interrupt();
        }
    }

    public boolean isAlive() {
        if (scriptThread != null) {
            return scriptThread.isAlive();
        } else {
            return false;
        }
    }

    public void join(long timeoutMillis) throws InterruptedException {
        if (scriptThread != null) {
            scriptThread.join(timeoutMillis);
        }
    }

    public void join() throws InterruptedException {
        join(0);
    }

    public Consumer<String> getStandardOutput() {
        return standardOutput;
    }

    public void setStandardOutput(Consumer<String> standardOutput) {
        this.standardOutput = standardOutput;
    }

    public Consumer<String> getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(Consumer<String> errorOutput) {
        this.errorOutput = errorOutput;
    }

    public void runScriptAsync(String script, String nuixVersion, Map<String, Object> variables) {
        initialize(nuixVersion, variables);

        scriptThread = new Thread(() -> {
            scriptingContainer.runScriptlet(script);
        });

        scriptThread.start();
    }

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
