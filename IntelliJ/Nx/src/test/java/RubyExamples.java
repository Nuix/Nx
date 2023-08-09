import com.nuix.innovation.enginewrapper.NuixEngine;
import com.nuix.innovation.enginewrapper.NuixLicenseResolver;
import com.nuix.innovation.enginewrapper.RubyScriptRunner;
import com.nuix.logging.LogHelper;
import nuix.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RubyExamples {
    protected static Logger log;
    protected static File testOutputDirectory;
    protected static File testDataDirectory;
    protected static File rubyExamplesDirectory;
    protected static boolean deleteTestOutputOnCompletion = true;

    @BeforeAll
    public static void setup() throws Exception {
        log = LogManager.getLogger(RubyExamples.class);

//        System.out.println("JVM Arguments:");
//        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
//        List<String> jvmArgs = bean.getInputArguments();
//        for (String arg : jvmArgs) {
//            System.out.println(arg);
//        }
//
//        System.out.println("Environment Variables:");
//        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
//            System.out.println(String.format("%s => %s", entry.getKey(), entry.getValue()));
//        }

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

        LogHelper.getInstance().attachConsoleAppender(null, (event) -> true);
        LogHelper.getInstance().setRootLoggerLevelInfo();
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

        NuixEngine engine = NuixEngine.usingFirstAvailableLicense(caseCreationCloud)
                .setEngineDistributionDirectoryFromEnvVar()
                .setLogDirectory(new File(testOutputDirectory, "Logs_" + System.currentTimeMillis()));
        engine.showConfidentialValuesInLog(true);
        return engine;
    }

    public void executeRubyFileInNuix(File rubyScriptFile, Map<String, Object> variables) throws Exception {
        log.info("Preparing to run: " + rubyScriptFile.getCanonicalPath());
        try (NuixEngine nuixEngine = constructNuixEngine()) {
            Utilities utilities = nuixEngine.getUtilities();
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put("$utilities", utilities);
            RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
            rubyScriptRunner.setStandardOutput(log::info);
            rubyScriptRunner.setErrorOutput(log::error);
            rubyScriptRunner.runFileAsync(rubyScriptFile, nuixEngine.getNuixVersionString(), variables);
            rubyScriptRunner.join();
        }
    }

    public void executeRubyExampleInNuix(String exampleFileName, Map<String, Object> variables) throws Exception {
        File exampleFile = new File(rubyExamplesDirectory, exampleFileName);
        executeRubyFileInNuix(exampleFile, variables);
    }

    public void executeRubyFile(File rubyScriptFile, Map<String, Object> variables) throws Exception {
        log.info("Preparing to run: " + rubyScriptFile.getCanonicalPath());
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("$utilities", "");
        RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
        rubyScriptRunner.setStandardOutput(log::info);
        rubyScriptRunner.setErrorOutput(log::error);
        rubyScriptRunner.runFileAsync(rubyScriptFile, "9000.0.0.0", variables);
        rubyScriptRunner.join();
    }

    public void executeRubyExample(String exampleFileName, Map<String, Object> variables) throws Exception {
        File exampleFile = new File(rubyExamplesDirectory, exampleFileName);
        executeRubyFile(exampleFile, variables);
    }

    public File generateCaseDirectory() throws IOException {
        return new File(testOutputDirectory, "Case" + System.currentTimeMillis()).getCanonicalFile();
    }

    @Test
    public void TestDynamicTable01() throws Exception {
        executeRubyExample("DynamicTableTest01.rb", null);
    }

    @Test
    public void TestDynamicTable02() throws Exception {
        executeRubyExample("DynamicTableTest02.rb", null);
    }

    @Test
    public void TestDynamicTable03() throws Exception {
        executeRubyExample("DynamicTableTest03.rb", null);
    }

    @Test
    public void TestChoiceDialog() throws Exception {
        executeRubyExample("ChoiceDialog.rb", null);
    }

    @Test
    public void TestCommonDialogs() throws Exception {
        executeRubyExample("CommonDialogs.rb", null);
    }

    @Test
    public void TestCsvTable() throws Exception {
        executeRubyExample("CsvTableTest.rb", null);
    }

    @Test
    public void TestMimeTypeSelectionDynamicTable() throws Exception {
        executeRubyExample("MimeTypeSelectionDynamicTable.rb", null);
    }

    @Test
    public void TestIngestionProgressDialog() throws Exception {
        File testCaseDirectory = generateCaseDirectory();
        testCaseDirectory.getParentFile().mkdirs();
        File testEvidenceDirectory = rubyExamplesDirectory.getCanonicalFile();
        Map<String, Object> vars = new HashMap<>();
        vars.put("test_case_location", testCaseDirectory);
        vars.put("test_evidence_location", testEvidenceDirectory);
        executeRubyExampleInNuix("IngestionProgressDialog.rb", vars);
    }

    @Test
    public void TestProgressDialog() throws Exception {
        executeRubyExample("ProgressDialog.rb", null);
    }

    @Test
    public void TestProgressDialogBasicAbortExample() throws Exception {
        executeRubyExample("ProgressDialogBasicAbortExample.rb", null);
    }

    @Test
    public void TestProgressDialogProcessingAbortExample() throws Exception {
        File testCaseDirectory = generateCaseDirectory();
        testCaseDirectory.getParentFile().mkdirs();
        File testEvidenceDirectory = rubyExamplesDirectory.getCanonicalFile();
        Map<String, Object> vars = new HashMap<>();
        vars.put("case_directory", testCaseDirectory);
        vars.put("source_data_path", testEvidenceDirectory);
        executeRubyExampleInNuix("ProgressDialogProcessingAbortExample.rb", vars);
    }

    @Test
    public void TestProgressDialogWithReport() throws Exception {
        executeRubyExample("ProgressDialogWithReport.rb", null);
    }

    @Test
    public void TestScrollableTabbedCustomDialog() throws Exception {
        File testCaseDirectory = generateCaseDirectory();
        testCaseDirectory.getParentFile().mkdirs();
        File testEvidenceDirectory = rubyExamplesDirectory.getCanonicalFile();
        Map<String, Object> vars = new HashMap<>();
        vars.put("case_directory", testCaseDirectory);
        executeRubyExampleInNuix("ScrollableTabbedCustomDialog.rb", vars);
    }

    @Test
    public void TestTabbedCustomDialog() throws Exception {
        File testCaseDirectory = generateCaseDirectory();
        testCaseDirectory.getParentFile().mkdirs();
        File testEvidenceDirectory = rubyExamplesDirectory.getCanonicalFile();
        Map<String, Object> vars = new HashMap<>();
        vars.put("case_directory", testCaseDirectory);
        executeRubyExampleInNuix("TabbedCustomDialog.rb", vars);
    }

    @Test
    public void TestButtonRow() throws Exception {
        executeRubyExample("ButtonRow.rb", null);
    }

    @Test
    public void TestDynamicTableCustomFiltering() throws Exception {
        executeRubyExample("DynamicTableCustomFiltering.rb", null);
    }

    @Test
    public void TestEnabledDependsOnCheckedStates() throws Exception {
        File testCaseDirectory = generateCaseDirectory();
        testCaseDirectory.getParentFile().mkdirs();
        File testEvidenceDirectory = rubyExamplesDirectory.getCanonicalFile();
        Map<String, Object> vars = new HashMap<>();
        vars.put("case_directory", testCaseDirectory);
        executeRubyExampleInNuix("EnabledDependsOnCheckedStates.rb", vars);
    }

    @Test
    public void TestHighRecordCountChoiceTable() throws Exception {
        executeRubyExample("HighRecordCountChoiceTable.rb", null);
    }

    @Test
    public void TestToasts() throws Exception {
        executeRubyExample("Toasts.rb", null);
    }
}
