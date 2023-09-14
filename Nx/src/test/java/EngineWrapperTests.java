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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EngineWrapperTests {
    protected static Logger log;
    protected static File testOutputDirectory;
    protected static File testDataDirectory;
    protected static boolean deleteTestOutputOnCompletion = true;

    @BeforeAll
    public static void setup() throws Exception {
        log = LogManager.getLogger("Tests");

        System.out.println("JVM Arguments:");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        for (String arg : jvmArgs) {
            System.out.println("\t" + arg);
        }

        System.out.println("Runtime Configuration:");
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        arguments.forEach(System.out::println);

        System.out.println("Environment Variables:");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            System.out.println(String.format("\t%s => %s", entry.getKey(), entry.getValue()));
        }

        testOutputDirectory = new File(System.getenv("TEST_OUTPUT_DIRECTORY"));
        testDataDirectory = new File(System.getenv("TEST_DATA_DIRECTORY"));

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

    @Test
    public void GetLicenseTryWithResourcesCleanup() throws Exception {
        // Create engine instance using try-with-resources, get utilities, use, closes
        // at end of try-with-resources
        try (NuixEngine nuixEngine = constructNuixEngine()) {
            Utilities utilities = nuixEngine.getUtilities();
            utilities.getItemTypeUtility().getAllTypes();
        }
    }

    @Test
    public void TestBasicRubyScript() throws Exception {
        List<String> outputLines = new ArrayList<>();
        try (NuixEngine nuixEngine = constructNuixEngine()) {
            Utilities utilities = nuixEngine.getUtilities();
            Map<String, Object> globalVars = Map.of("$utilities", utilities);
            String script = "$utilities.getItemTypeUtility.getAllKinds.each{|kind| puts kind.getName}";
            RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
            rubyScriptRunner.setStandardOutputConsumer(outputLines::add);
            rubyScriptRunner.setErrorOutputConsumer(outputLines::add);
            rubyScriptRunner.runScriptAsync(script, nuixEngine.getNuixVersionString(), globalVars);
            rubyScriptRunner.join();
            log.info("Script Output:");
            log.info(String.join("",outputLines));
            assertTrue(outputLines.size() > 0);
        }
    }
}
