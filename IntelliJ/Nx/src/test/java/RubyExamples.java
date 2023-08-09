import com.nuix.innovation.enginewrapper.NuixEngine;
import com.nuix.innovation.enginewrapper.NuixLicenseResolver;
import com.nuix.innovation.enginewrapper.RubyScriptRunner;
import nuix.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RubyExamples {
    protected static Logger log;
    protected static File testOutputDirectory;
    protected static File testDataDirectory;
    protected static File rubyExamplesDirectory;
    protected static boolean deleteTestOutputOnCompletion = true;

    @BeforeAll
    public static void setup() throws Exception {
        log = LogManager.getLogger(RubyExamples.class);

        System.out.println("JVM Arguments:");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        for (String arg : jvmArgs) {
            System.out.println(arg);
        }

        System.out.println("Environment Variables:");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            System.out.println(String.format("%s => %s", entry.getKey(), entry.getValue()));
        }

        testOutputDirectory = new File(System.getenv("TEST_OUTPUT_DIRECTORY"));
        testDataDirectory = new File(System.getenv("TEST_DATA_DIRECTORY"));
        rubyExamplesDirectory = new File(System.getenv("RUBY_EXAMPLES_DIRECTORY"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (deleteTestOutputOnCompletion) {
                try {
                    FileUtils.deleteDirectory(testOutputDirectory);
                } catch (IOException exc) {
                    log.error("Error while deleting output directory: " + testOutputDirectory.getAbsolutePath(), exc);
                }
            }
        }));
    }

    @AfterAll
    public static void breakdown() {
        NuixEngine.closeGlobalContainer();
    }

    public NuixEngine constructNuixEngine(String... additionalRequiredFeatures) {
        List<String> features = List.of("CASE_CREATION");
        if (additionalRequiredFeatures != null && additionalRequiredFeatures.length > 0) {
            features.addAll(List.of(additionalRequiredFeatures));
        }

        NuixLicenseResolver caseCreationCloud = NuixLicenseResolver.fromCloud()
                .withLicenseCredentialsResolvedFromEnvVars()
                .withMinWorkerCount(4)
                .withRequiredFeatures(features);

        return NuixEngine.usingFirstAvailableLicense(caseCreationCloud)
                .setEngineDistributionDirectoryFromEnvVar()
                .setLogDirectory(new File(testOutputDirectory, "Logs_" + System.currentTimeMillis()));
    }

    public void executeRubyFileInNuix(File rubyScriptFile) throws Exception {
        log.info("Preparing to run: " + rubyScriptFile.getCanonicalPath());
        List<String> outputLines = new ArrayList<>();
        try (NuixEngine nuixEngine = constructNuixEngine()) {
            Utilities utilities = nuixEngine.getUtilities();
            Map<String, Object> globalVars = Map.of(
                    "$utilities", utilities
            );
            RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
            rubyScriptRunner.setStandardOutput(outputLines::add);
            rubyScriptRunner.setErrorOutput(outputLines::add);
            rubyScriptRunner.runFileAsync(rubyScriptFile, nuixEngine.getNuixVersionString(), globalVars);
            rubyScriptRunner.join();
            log.info("Script Output:");
            log.info(String.join("", outputLines));
        }
    }

    public void executeRubyExampleInNuix(String exampleFileName) throws Exception {
        File exampleFile = new File(rubyExamplesDirectory, exampleFileName);
        executeRubyFileInNuix(exampleFile);
    }

    public void executeRubyFile(File rubyScriptFile) throws Exception {
        log.info("Preparing to run: " + rubyScriptFile.getCanonicalPath());
        List<String> outputLines = new ArrayList<>();
        Map<String, Object> globalVars = Map.of(
                "$utilities", ""
        );
        RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
        rubyScriptRunner.setStandardOutput(outputLines::add);
        rubyScriptRunner.setErrorOutput(outputLines::add);
        rubyScriptRunner.runFileAsync(rubyScriptFile, "9000.0.0.0", globalVars);
        rubyScriptRunner.join();
        log.info("Script Output:");
        log.info(String.join("", outputLines));
    }

    public void executeRubyExample(String exampleFileName) throws Exception {
        File exampleFile = new File(rubyExamplesDirectory, exampleFileName);
        executeRubyFile(exampleFile);
    }

    @Test
    public void TestDynamicTable01() throws Exception {
        executeRubyExample("DynamicTableTest.rb");
    }
}
