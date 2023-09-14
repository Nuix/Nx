package com.nuix.innovation.enginewrapper;

import com.google.common.base.Suppliers;
import nuix.ThirdPartyDependency;
import nuix.ThirdPartyDependencyStatus;
import nuix.Utilities;
import nuix.engine.Engine;
import nuix.engine.GlobalContainer;
import nuix.engine.GlobalContainerFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/***
 * This class represents a wrapper over the Nuix Engine API.  It encapsulates the potentially error prone process of getting
 * a Nuix Engine instance initialized and licensed with a simplified interface.  Use this class as is or use it as a
 * starting point for your own implementation.<br><br>
 * Basic usage example:
 * <pre>
 * {@code
 * // Define a resolver which will resolve licenses from Cloud License Server (CLS),
 * // authenticating using upon environment variable "NUIX_USERNAME" and "NUIX_PASSWORD",
 * // that have at least 4 workers and the feature "CASE_CREATION".
 * LicenseResolver cloud_4_workers = NuixLicenseResolver.fromCloud()
 *     .withLicenseCredentialsResolvedFromEnvVars()
 *     .withMinWorkerCount(4)
 *     .withRequiredFeatures("CASE_CREATION");
 *
 * // Define a resolver which will attempt to resolve a license from a local physical dongle
 * // that has the feature "CASE_CREATION".
 * LicenseResolver anyDongle = NuixLicenseResolver.fromDongle()
 *     .withRequiredFeatures("CASE_CREATION");
 *
 * // Create a new NuixEngine instance which will first attempt to resolve a cloud license and then
 * // attempt to resolve a dongle license if one cannot be resolved from cloud, depending resolvers
 * // defined above.  Calling run method to execute code with a licensed Engine instance (if a license can be obtained).
 * NuixEngine.usingFirstAvailableLicense(cloud_4_workers, anyDongle)
 *     .setEngineDistributionDirectoryFromEnvVars()
 *     .run((utilities -> {
 *         log.info("License was obtained!");
 *         // Do something with Utilities and Engine here
 *     }));
 * }
 * </pre>
 * @author Jason Wells
 */
public class NuixEngine implements AutoCloseable {
    private static GlobalContainer globalContainer = null;
    protected Supplier<File> engineDistributionDirectorySupplier;
    protected Supplier<File> logDirectorySupplier;
    protected Supplier<File> userDataDirectorySupplier;
    protected List<LicenseResolver> nuixLicenseResolvers;

    protected Logger log = null;
    protected Engine engine = null;
    protected Utilities utilities = null;
    protected Thread shutdownHook = null;

    protected NuixEngine() {
    }

    public static void closeGlobalContainer() {
        if (globalContainer != null) {
            globalContainer.close();
            globalContainer = null;
        }
    }

    /***
     * Creates a new instance which will attempt to retrieve its license from one of the provided {@link LicenseResolver}
     * instances in the order specified.
     * @param nuixLicenseResolvers One or more resolvers which will be called upon in the order provided to obtain a license
     *                         until one is able to successfully acquire an available license based on its configured source
     *                         and filtering/selection logic.
     * @return A new NuixEngine instance
     */
    public static NuixEngine usingFirstAvailableLicense(LicenseResolver... nuixLicenseResolvers) {
        return usingFirstAvailableLicense(Arrays.asList(nuixLicenseResolvers));
    }

    /***
     * Creates a new instance which will attempt to retrieve its license from one of the provided {@link LicenseResolver}
     * instances in the order specified.
     * @param nuixLicenseResolvers List of one or more resolvers which will be called upon in the order provided to obtain a license
     *                         until one is able to successfully acquire an available license based on its configured source
     *                         and filtering/selection logic.
     * @return A new NuixEngine instance
     */
    public static NuixEngine usingFirstAvailableLicense(List<LicenseResolver> nuixLicenseResolvers) {
        NuixEngine result = new NuixEngine();
        result.nuixLicenseResolvers = nuixLicenseResolvers;
        return result;
    }

    /***
     * Create a new instance which will attempt to retrieve its license from anywhere it can.
     * @return A new NuixEngine instance
     */
    public static NuixEngine usingAnyAvailableLicense() {
        NuixEngine result = new NuixEngine();
        result.nuixLicenseResolvers = List.of(NuixLicenseResolver.fromAnySource());
        return result;
    }

    /***
     * For various reasons, this class needs to be able to resolve the location of a Nuix Engine distribution.
     * This method allows you to provide a Supplier which will resolve it as needed.  Note that due to the importance
     * of being able to resolve this, you must configure this value via one of the following methods before calling
     * {@link #run(ThrowCapableConsumer)}, or you will get an error:
     * <ul>
     *     <li>{@link #setEngineDistributionDirectory(File)}</li>
     *     <li>{@link #setEngineDistributionDirectoryFromEnvVar(String)}</li>
     *     <li>{@link #setEngineDistributionDirectoryFromEnvVar()}</li>
     * </ul>
     * @param engineDistributionDirectorySupplier A supplier which will yield directory containing a Nuix Engine distribution.
     * @return This instance for method call chaining
     */
    public NuixEngine setEngineDistributionDirectorySupplier(Supplier<File> engineDistributionDirectorySupplier) {
        this.engineDistributionDirectorySupplier = Suppliers.memoize(engineDistributionDirectorySupplier::get);
        return this;
    }

    /***
     * For various reasons, this class needs to be able to resolve the location of a Nuix Engine distribution.
     * This method allows you to specify an explicit directory value.  Note that due to the importance
     * of being able to resolve this, you must configure this value via one of the following methods before calling
     * {@link #run(ThrowCapableConsumer)}, or you will get an error:
     * <ul>
     *    <li>{@link #setEngineDistributionDirectorySupplier(Supplier)}</li>
     *    <li>{@link #setEngineDistributionDirectoryFromEnvVar(String)}</li>
     *    <li>{@link #setEngineDistributionDirectoryFromEnvVar()}</li>
     *</ul>
     * @param directory The directory containing a Nuix Engine distribution
     * @return This instance for method call chaining
     */
    public NuixEngine setEngineDistributionDirectory(File directory) {
        setEngineDistributionDirectorySupplier(() -> directory);
        return this;
    }

    /***
     * For various reasons, this class needs to be able to resolve the location of a Nuix Engine distribution.
     * This method allows you to specify an environment variable which contains as its value the a directory
     * containing a Nuix Engine distribution.  Note that due to the importance
     *of being able to resolve this, you must configure this value via one of the following methods before calling
     *{@link #run(ThrowCapableConsumer)}, or you will get an error:
     *<ul>
     *   <li>{@link #setEngineDistributionDirectorySupplier(Supplier)}</li>
     *   <li>{@link #setEngineDistributionDirectory(File)}</li>
     *   <li>{@link #setEngineDistributionDirectoryFromEnvVar()}</li>
     *</ul>
     * @param environmentVariableName The name of the environment variable which has its value set to a directory containing
     *                                a Nuix Engine distribution.
     * @return This instance for method call chaining
     */
    public NuixEngine setEngineDistributionDirectoryFromEnvVar(String environmentVariableName) {
        setEngineDistributionDirectorySupplier(() -> {
            String envValue = System.getenv(environmentVariableName);
            System.out.printf("Obtained value '%s' from ENV var '%s'%n",
                    envValue, environmentVariableName);
            return new File(envValue);
        });
        return this;
    }

    /***
     * For various reasons, this class needs to be able to resolve the location of a Nuix Engine distribution.
     * This method allows you to specify that this directory should be obtained from the environment variable
     * named "NUIX_ENGINE_DIR".  Note that this is effectively just a convenience method to calling
     * {@link #setEngineDistributionDirectoryFromEnvVar(String)} with the value "NUIX_ENGINE_DIR".  Note that due to the importance
     * of being able to resolve this, you must configure this value via one of the following methods before calling
     * {@link #run(ThrowCapableConsumer)}, or you will get an error:
     * <ul>
     *    <li>{@link #setEngineDistributionDirectorySupplier(Supplier)}</li>
     *    <li>{@link #setEngineDistributionDirectory(File)}</li>
     *    <li>{@link #setEngineDistributionDirectoryFromEnvVar(String)}</li>
     * </ul>
     * @return This instance for method call chaining
     */
    public NuixEngine setEngineDistributionDirectoryFromEnvVar() {
        setEngineDistributionDirectoryFromEnvVar("NUIX_ENGINE_DIR");
        return this;
    }

    /***
     * When logging is initialized a directory is specified in which log files are to be written.  Calling this method
     * allows you to provide a Supplier which will yield that directory when needed.  Directory will be created if it
     * does not exist.  If neither this method nor the method {@link #setLogDirectory(File)} are called before logging
     * initialization, then a default will be assumed in the form of "%LOCAL_APP_DATA%\Nuix\Logs\Engine-[DATE]-[TIME]".
     * @param logDirectorySupplier A Supplier which will yield a log directory to use
     * @return This instance for method call chaining
     */
    public NuixEngine setLogDirectorySupplier(Supplier<File> logDirectorySupplier) {
        this.logDirectorySupplier = logDirectorySupplier::get;
        return this;
    }

    /***
     * When logging is initialized a directory is specified in which log files are to be written.  Calling this method
     * allows you to set an explicit directory to use.  Directory will be created if it does not exist.  If neither this
     * method nor the method {@link #setLogDirectorySupplier(Supplier)} are called before logging initialization,
     * then a default will be assumed in the form of "%LOCAL_APP_DATA%\Nuix\Logs\Engine-[DATE]-[TIME]".
     * @param directory The log file directory to use
     * @return This instance for method call chaining
     */
    public NuixEngine setLogDirectory(File directory) {
        setLogDirectorySupplier(() -> directory);
        return this;
    }

    /***
     * The Nuix Engine will need to be capable of resolving various artifacts such as metadata profiles, processing profiles,
     * export profiles, word lists, etc.  This method allows you to provide a Supplier which will yield a directory containing
     * this information as needed.  If neither this method nor the method {@link #setUserDataDirectory(File)} are called
     * before calling {@link #run}, the "user-data" subdirectory of the Nuix Engine distribution will be used.
     * @param userDataDirectorySupplier A Supplier which will yield directory containing engine user data
     * @return This instance for method call chaining
     */
    public NuixEngine setUserDataDirectorySupplier(Supplier<File> userDataDirectorySupplier) {
        System.out.println("Setting the supplier: " + userDataDirectorySupplier);
        this.userDataDirectorySupplier = Suppliers.memoize(userDataDirectorySupplier::get);
        return this;
    }

    /***
     * The Nuix Engine will need to be capable of resolving various artifacts such as metadata profiles, processing profiles,
     * export profiles, word lists, etc.  This method allows you to provide an explicit user data directory.
     * If neither this method nor the method {@link #setUserDataDirectorySupplier(Supplier)} are called
     * before calling {@link #run}, the "user-data" subdirectory of the Nuix Engine distribution will be used.
     * @param directory The user data directory to use
     * @return This instance for method call chaining
     */
    public NuixEngine setUserDataDirectory(File directory) {
        setLogDirectorySupplier(() -> directory);
        return this;
    }

    /***
     * Gets Utilities object to begin making use of the Nuix API.  If instance has been previously obtained, then
     * that instance will be returned.  Otherwise, calling this method performs a series of steps to get setup:
     * <ol>
     *     <li>A set of preconditions are checked.  Generally if a condition is not met an exception explaining the issue
     *     will be thrown.  In a couple instances a default will be assumed if possible.
     *     <ul>
     *         <li>Can we resolve an engine distribution?</li>
     *         <li>Can we resolve a log directory?</li>
     *         <li>Can we resolve a user data directory?</li>
     *         <li>Does the system PATH have a reference to "[ENGINE_DIR]\bin"?</li>
     *         <li>Does the system PATH have a reference to "[ENGINE_DIR]\bin\x86"?</li>
     *     </ul>
     *     </li>
     *     <li>Logging is initialized</li>
     *     <li>The Nuix Engine GlobaContainer instance is created if needed</li>
     *     <li>An Engine instance is created</li>
     *     <li>Any provided {@link NuixLicenseResolver} instances are iteratively called upon to obtain a license until an
     *     instance has successfully acquired one.
     *     </li>
     *     <li>The callback provided when calling this method is invoked with a licenses Utilities object if license
     *     acquisition was successful.</li>
     *     <li>
     *         If an exception is thrown during this process, it is caught, written to System.out and then rethrown
     *         to be further handled by the caller.
     *     </li>
     * </ol>
     * Note that before calling this method you must call one of the following methods to configure where a
     * Nuix Engine distribution is located:
     * <ul>
     *     <li>{@link #setEngineDistributionDirectorySupplier(Supplier)}</li>
     *     <li>{@link #setEngineDistributionDirectory(File)}</li>
     *     <li>{@link #setEngineDistributionDirectoryFromEnvVar(String)}</li>
     *     <li>{@link #setEngineDistributionDirectoryFromEnvVar()}</li>
     * </ul>
     *
     * @return If this instance already has an instance of Utilities, that is returned.  Otherwise necessary steps
     * will be taken to attempt to obtain and license underlying engine instance to ultimately provide a licensed
     * Utilities instance.
     * @throws Exception Allows exceptions to bubble up so caller can handle them.
     */
    public Utilities getUtilities() throws Exception {
        if (utilities == null) {
            // Check to make sure some requirements are in place before proceeding
            checkPreConditions();

            // Make sure logging gets initialized
            try {
                initializeLogging();
            } catch (Exception exc) {
                System.out.println("Error while initializing logging: " + exc.getMessage());
                throw new Exception("Error while initializing logging", exc);
            }

            // Proceed with constructing engine instance, obtaining license and providing licensed Utilities
            // to provided callback
            log.info("Engine Distribution Directory: " + engineDistributionDirectorySupplier.get().getAbsolutePath());
            log.info("Log Directory: " + logDirectorySupplier.get().getAbsolutePath());
            log.info("User Data Directory: " + userDataDirectorySupplier.get().getAbsolutePath());

            ensureGlobalContainer();
            buildEngine();
            if (obtainLicenseFromResolvers()) {
                utilities = engine.getUtilities();
                logAllDependencyInfo(utilities);
            } else {
                log.error("No license was able to be resolved");
            }
        }

        return utilities;
    }

    /***
     * Convenience method for running an operation with a licensed engine instance and then automatically closing this instance.
     * Supplied consumer will be provided a utilities instance by internally calling {@link #getUtilities()}.
     * Upon return from consumer, either from normal return or exception, a 'finally' block will call the
     * {@link #close()} method for you.  Exceptions will be allowed to bubble up, so caller can handle them directly.
     * @param throwCapableConsumer A callback which is to receive Utilities upon successful initialization and licensing.
     * @throws Exception May be thrown by process or getting engine initialized or code in supplied consumer.
     */
    public void run(ThrowCapableConsumer<Utilities> throwCapableConsumer) throws Exception {
        try {
            throwCapableConsumer.accept(getUtilities());
        } finally {
            close();
        }
    }

    /***
     * When creating a new instance via {@link NuixEngine#usingFirstAvailableLicense(LicenseResolver...)}, caller can
     * specify a series of {@link NuixLicenseResolver} instances which will be called upon in sequence until one acquires
     * a license.  This method iterates through those resolvers to make that happen.
     * @return True if a license was obtained, false if not.
     * @throws Exception This method does not throw any methods itself, but instead allows any thrown methods to bubble up.
     */
    private boolean obtainLicenseFromResolvers() throws Exception {
        boolean licenseWasObtained = false;
        // Iterate each provided license resolver in order until one signals to use it has licensed
        // our engine instance.
        for (LicenseResolver resolver : nuixLicenseResolvers) {
            log.info(String.format("Attempting to resolve license using: %s", resolver));
            licenseWasObtained = resolver.resolveLicense(engine);
            if (licenseWasObtained) {
                log.info(String.format("Obtained license: %s", NuixLicenseFeaturesLogger.summarizeLicense(engine.getLicence())));
                break;
            } else {
                log.info("No license was obtained, will try next resolver if there is one");
            }
        }
        return licenseWasObtained;
    }

    /***
     * This method checks to ensure various things are configured early on in the engine initialization process.  We want
     * to detect common misconfigurations here and when detected report the issue.
     * @throws Exception This method may throw an exception if something is not configured correctly or at all and a default
     * cannot be somehow assumed.  This includes:
     * <ul>
     *     <li>Unable to resolve a Nuix Engine distribution</li>
     *     <li>Unable to resolve 'bin' dir on PATH</li>
     *     <li>Unable to resolve 'bin\x86' dir on PATH</li>
     * </ul>
     */
    private void checkPreConditions() throws Exception {
        // Due to changes in later versions of Java coupled with some added functionality
        // added to Nuix at one point, we need to make sure the JVM we are running in
        // was started with this arg:
        // --add-exports=java.base/jdk.internal.loader=ALL-UNNAMED
        // If it was not, then you will likely get error similar to this:
        // java.lang.NoClassDefFoundError: org/bouncycastle/openpgp/PGPException
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        final String requiredJvmArg = "--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED";
        if (jvmArgs.stream().noneMatch(arg -> arg.trim().equalsIgnoreCase(requiredJvmArg))) {
            throw new IllegalStateException("Please ensure that JVM is started with argument: " + requiredJvmArg);
        }

        // Caller must have set engine distribution directory, fail if they have not.
        if (engineDistributionDirectorySupplier == null) {
            throw new IllegalStateException("Unable to resolve engine distribution directory, please call one of the following methods " +
                    "before calling the run method: " +
                    "setEngineDistributionDirectorySupplier, setEngineDistributionDirectory, setEngineDistributionDirectoryFromENV");
        }

        // We need to set JVM system property 'nuix.libdir' so engine can resolve some things early on.
        // Without this you may see an error about fips/non-fips module resolution
        File libDir = new File(engineDistributionDirectorySupplier.get(), "lib");
        System.setProperty("nuix.libdir", libDir.getAbsolutePath());

        // If caller has not configured a log directory, attempt to guess one in local app data, otherwise throw exception.
        if (logDirectorySupplier == null) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isEmpty()) {
                File localAppDataDirectory = new File(localAppData);
                File logDirectory = new File(localAppDataDirectory, "Engine-" + DateTime.now().toString("YYYYMMdd-HHmmss"));
                System.out.println("No log directory specified, assuming local app data log directory: " + logDirectory.getAbsolutePath());
                setLogDirectory(logDirectory);
            } else {
                throw new IllegalStateException("Unable to resolve log directory, please call either " +
                        "setLogDirectorySupplier or setLogDirectory method before calling run method");
            }
        }

        // If we reached here, we should have been able to resolve a log directory.  Let's make sure that directory
        // exists so later during logging initialization we don't receive an exception about non-existent directory.
        logDirectorySupplier.get().getCanonicalFile().mkdirs();
        if (!logDirectorySupplier.get().getCanonicalFile().exists()) {
            throw new IOException("Unable to create log directory: " + logDirectorySupplier.get().getCanonicalPath());
        }

        // If caller has not specified a user-data directory, assume the one that comes with the engine distribution
        // that is being used.
        if (userDataDirectorySupplier == null) {
            System.out.println("No user data directory was specified, assuming directory relative to engine distribution: " +
                    new File(engineDistributionDirectorySupplier.get(), "user-data").getAbsolutePath());
            userDataDirectorySupplier = () -> new File(engineDistributionDirectorySupplier.get(), "user-data");
        }

        // Make sure PATH points to expected bin and bin/x86 subdirectories of our engine distribution
        String[] pathDirs = System.getenv("PATH").split(";");
        File expectedBinDir = new File(engineDistributionDirectorySupplier.get(), "bin");
        File expectedBinX86Dir = new File(expectedBinDir, "x86");

        // Make sure we can locate 'bin' subdirectory on PATH
        if (Arrays.stream(pathDirs).noneMatch(pathDir -> pathDir.equalsIgnoreCase(expectedBinDir.getAbsolutePath()))) {
            throw new IllegalStateException("PATH does not contain expected 'bin' directory: " + expectedBinDir.getAbsolutePath());
        } else {
            System.out.println("'bin' Successfully found on PATH: " + expectedBinDir.getAbsolutePath());
        }

        // Make sure we can locate 'bin\x86' subdirectory on PATH
        if (Arrays.stream(pathDirs).noneMatch(pathDir -> pathDir.equalsIgnoreCase(expectedBinX86Dir.getAbsolutePath()))) {
            throw new IllegalStateException("PATH does not contain expected 'bin\\x86' directory: " + expectedBinX86Dir.getAbsolutePath());
        } else {
            System.out.println("'bin\\x86' Successfully found on PATH: " + expectedBinX86Dir.getAbsolutePath());
        }
    }

    /***
     * Initializes some logging details.
     */
    protected void initializeLogging() {
        if (log == null) {
            System.setProperty("nuix.logdir", logDirectorySupplier.get().getAbsolutePath());
            // Use Log4j2 config YAML from engine base directory
            File log4jConfigFile = new File(engineDistributionDirectorySupplier.get(), "config/log4j2.yml");
            System.setProperty("log4j.configurationFile", log4jConfigFile.getAbsolutePath());
            log = LogManager.getLogger(this.getClass());
            log.info("log4j.configurationFile => " + log4jConfigFile.getAbsolutePath());

            // Set default level to INFO
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.INFO);
            ctx.updateLoggers();
        }
    }

    /***
     * If we do not yet have a global container instance, creates one.
     */
    protected void ensureGlobalContainer() {
        if (globalContainer == null) {
            globalContainer = GlobalContainerFactory.newContainer();
        }
    }

    /***
     * Builds an engine instance.  Also registers a shutdown hook will attempt to release any license held by
     * this instance in some situations which my not otherwise be handled cleanly.  If this shutdown hook was not
     * in place it is possible for a license to remain claimed after the claiming process has ended and until a timeout
     * period has elapsed for the license.
     */
    protected void buildEngine() {
        Map<Object, Object> engineConfiguration = Map.of(
                "user", System.getProperty("user.name"),
                "userDataDirs", userDataDirectorySupplier.get()
        );

        engine = globalContainer.newEngine(engineConfiguration);
        log.info("Obtained Engine instance v" + engine.getVersion());

        // Whenever we create an instance of the engine to hand over to the user, we will register
        // our shutdown hook to close this instance.  This helps to ensure that the license is released
        // in some scenarios where our code to explicitly release it may be skipped, such as code calling
        // System.exit before returning from user provided Consumer<Utilities>.
        log.info("Adding shutdown hook for EngineWrapper::close");
        shutdownHook = new Thread(() -> {
            try {
                close();
            } catch (Exception e) {
                log.error("Error in shutdown hook", e);
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /***
     * Returns the Nuix engine version by internally calling Engine.getVersion
     * <br>Note that if Engine instance has not yet been initialized this returns "0.0.0.0"
     * @return A String representing the Nuix Engine version or "0.0.0.0" if Engine has not yet been initialized.
     */
    public String getNuixVersionString() {
        if (engine != null) {
            return engine.getVersion();
        } else {
            return "0.0.0.0";
        }
    }

    /***
     * Gets a {@link NuixVersion} object representing the Engine version as obtained by calling Engine.getVersion
     * <br>Note that if Engine instance has not yet been initialized this returns "0.0.0.0"
     * @return A NuixVersion object representing the Nuix Engine version or one representing "0.0.0.0" if
     * Engine has not yet been initialized.
     */
    public NuixVersion getNuixVersion() {
        return NuixVersion.parse(getNuixVersionString());
    }

    /***
     * Logs information about all Nuix third party dependencies
     * @param utilities Needs an instance of Utilities to get access to third party dependency information
     */
    protected void logAllDependencyInfo(Utilities utilities) {
        log.info("Reviewing third party dependency statuses:");
        try {
            List<ThirdPartyDependency> dependencies = utilities.getThirdPartyDependencies();
            for (ThirdPartyDependency dependency : dependencies) {
                try {
                    ThirdPartyDependencyStatus status = dependency.performCheck();
                    log.info(String.format(
                            "[%s] '%s': %s",
                            status.isAttentionRequired() ? " " : "X",
                            dependency.getDescription(),
                            status.getMessage()
                    ));
                } catch (Exception e) {
                    log.error(String.format(
                            "[!] '%s': Error Checking Status: %s",
                            dependency.getDescription(),
                            e.getMessage()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Error while fetching list of third party dependencies", e);
        }
    }

    /***
     * Cleans up resources associated with this instance:
     * <ul>
     *     <li>Calls close on underlying Engine instance</li>
     *     <li>Drop reference to obtained Utilities object</li>
     *     <li>Unregisters shutdown hook</li>
     *     <li>Shuts down logging</li>
     * </ul>
     * @throws Exception If thrown, was a result of a method being called by this method and allowed to bubble up to caller.
     */
    @Override
    public void close() throws Exception {
        // Close engine if we have an instance to close
        if (engine != null) {
            final String message = "Closing engine instance";
            if (log != null) {
                log.info(message);
            } else {
                System.out.println(message);
            }
            engine.close();
        }

        // Drop reference to Utilities object
        utilities = null;

        // Unregister shutdown hook since we are closing things up now
        if (shutdownHook != null) {
            final String message = "Removing shutdown hook to NuixEngine::close";
            if (log != null) {
                log.info(message);
            } else {
                System.out.println(message);
            }
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            shutdownHook = null;
        }

        // Shutdown logging
        if (log != null) {
            ((LifeCycle) LogManager.getContext()).stop();
            log = null;
        }
    }

    public void showConfidentialValuesInLog(boolean enabled) {
        System.setProperty("nuix.log.confidential.showValues", String.valueOf(enabled));
    }
}