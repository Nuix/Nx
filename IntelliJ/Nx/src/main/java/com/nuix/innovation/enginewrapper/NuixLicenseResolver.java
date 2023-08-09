package com.nuix.innovation.enginewrapper;

import lombok.NonNull;
import nuix.LicenceProperties;
import nuix.engine.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/***
 * This class provides license resolution for a {@link NuixEngine} instance.  Use it to specify:
 * <ul>
 *     <li>What license sources should be queried:<ul>
 *         <li>{@link NuixLicenseResolver#fromServer(String, int)}</li>
 *         <li>{@link NuixLicenseResolver#fromCloud()}</li>
 *         <li>{@link NuixLicenseResolver#fromDongle()}</li>
 *         <li>{@link NuixLicenseResolver#fromAnySource()}</li>
 *     </ul></li>
 *     <li>Criteria to disqualify some licenses from being acquired when there are multiple to select from:<ul>
 *         <li>{@link #withRequiredFeatures(String...)}</li>
 *         <li>{@link #withMinWorkerCount(int)}</li>
 *         <li>{@link #withMaxWorkerCount(int)}</li>
 *         <li>{@link #withFinalDecisionMadeBy(Function)}</li>
 *     </ul></li>
 *     <li>Custom logic to ultimately pick the license acquired from candidates {@link #withFinalDecisionMadeBy(Function)}</li>
 *     <li>Configuration details that are part of obtaining a license:<ul>
 *         <li>{@link #withCertificateTrustCallback(CertificateTrustCallback)}</li>
 *         <li>{@link #withLicenseCredentials(String, String)}</li>
 *         <li>{@link #withLicenseCredentialsProvider(CredentialsCallback)}</li>
 *         <li>{@link #withLicenseCredentialsResolvedFromEnvVars(String, String)}</li>
 *         <li>{@link #withLicenseCredentialsResolvedFromEnvVars()}</li>
 *     </ul></li>
 * </ul>
 *<br><br>
 * Example usages:
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
 * }
 * </pre>
 * @author Jason Wells
 */
public class NuixLicenseResolver implements LicenseResolver {
    private static final Logger log = LogManager.getLogger(NuixLicenseResolver.class);

    /***
     * An enum with options about how this license resolver should locate its license.
     */
    public enum LicenseResolutionSource {
        /***
         * The license should be resolved from physical dongles attached to the machine.
         */
        Dongle,
        /***
         * The license should be resolved from a Nuix Management Server (NMS) instance.
         */
        Server,
        /***
         * The license should be resolved from the Nuix Cloud License Server (CLS).
         */
        Cloud,
        /***
         * The license resolution is not constrained to any particular source.
         */
        Any,
        /***
         * Similar to Any, but the user provides a custom list of sources.
         */
        Custom
    }

    protected LicenseResolutionSource licenseSource;
    protected String customSource;
    protected String serverHost;
    protected int serverPort = 27443;
    protected CredentialsCallback credentialsCallback;
    protected CertificateTrustCallback certificateTrustCallback;
    protected Set<String> requiredFeatures = new HashSet<>();
    protected int minWorkerCount = 0;
    protected int maxWorkerCount = 0;
    protected String targetShortName = null;
    protected Function<Stream<AvailableLicence>, Optional<AvailableLicence>> finalDecider;

    protected NuixLicenseResolver() {
        // By default, we will just return the first one that matches our criteria, but user
        // could provide custom logic that ultimately decides which license (if any) of those
        // that have met other criteria is selected.
        finalDecider = availableLicenceStream -> {
            log.info("Picking first available license candidate...");
            return availableLicenceStream.findAny();
        };

        // By default, we blindly trust any cert.  User can provide more discerning method if they wish.
        certificateTrustCallback = certificateTrustCallbackInfo -> {
            log.info("Blindly trusting certificate...");
            certificateTrustCallbackInfo.setTrusted(true);
        };
    }

    /***
     * Creates an instance specifically for resolving a license from a Nuix Management Server (NMS) instance.
     * @param host The host/ip of the NMS server.
     * @param port The port of the NMS server.
     * @return An NMS specific license resolver
     */
    public static NuixLicenseResolver fromServer(@NonNull String host, int port) {
        NuixLicenseResolver result = new NuixLicenseResolver();
        result.licenseSource = LicenseResolutionSource.Server;
        result.serverHost = host;
        result.serverPort = port;
        return result;
    }

    /***
     * Creates an instance specifically for resolving a license from a Nuix Management Server (NMS) instance.
     * Similar to calling {@link #fromServer(String, int)} with a port of 27443 (the default).
     * @param host The host/ip of the NMS server.
     * @return An NMS specific license resolver
     */
    public static NuixLicenseResolver fromServer(@NonNull String host) {
        return fromServer(host, 27443);
    }

    /***
     * Creates an instance specifically for resolving a physical dongle based license.
     * @return A dongle specific license resolver.
     */
    public static NuixLicenseResolver fromDongle() {
        NuixLicenseResolver result = new NuixLicenseResolver();
        result.licenseSource = LicenseResolutionSource.Dongle;
        return result;
    }

    /***
     * Creates an instance specifically for resolving a license from the Nuix Cloud License Server (CLS).
     * @return A CLS specific license resolver.
     */
    public static NuixLicenseResolver fromCloud() {
        NuixLicenseResolver result = new NuixLicenseResolver();
        result.licenseSource = LicenseResolutionSource.Cloud;
        return result;
    }

    /***
     * Creates an instance which places no constraints on where the license is obtained from.  When using this,
     * no "sources" parameter is provided to the licensor, allowing for a license to be obtained from all sources
     * that the engine is able to locate.
     * @return A license resolver that is indifferent to where the license comes from.
     */
    public static NuixLicenseResolver fromAnySource() {
        NuixLicenseResolver result = new NuixLicenseResolver();
        result.licenseSource = LicenseResolutionSource.Any;
        return result;
    }

    /***
     * Creates an instance that will specify a custom source.
     * @param customSource The custom source value to provide.
     * @return A license resolver that uses a custom license source.
     */
    public static NuixLicenseResolver fromCustomSource(@NonNull String customSource) {
        NuixLicenseResolver result = new NuixLicenseResolver();
        result.licenseSource = LicenseResolutionSource.Custom;
        result.customSource = customSource;
        return result;
    }

    /***
     * Specifies a list of one or more features that a license must have to be acceptable.
     * @param features The required features.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withRequiredFeatures(String... features) {
        if (features != null) {
            requiredFeatures.addAll(Arrays.asList(features));
        }
        return this;
    }

    /***
     * Specifies a list of one or more features that a license must have to be acceptable.
     * @param features The required features.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withRequiredFeatures(Collection<String> features) {
        if (features != null) {
            requiredFeatures.addAll(features);
        }
        return this;
    }

    /***
     * Specifies a minimum worker count a given license must have to be acceptable.
     * @param minWorkerCount The minimum worker count.  A value of 0 means no minimum.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withMinWorkerCount(int minWorkerCount) {
        this.minWorkerCount = minWorkerCount;
        return this;
    }

    /***
     * Specifies a maximum worker count a license can have to be acceptable.  This exists mostly
     * to help in situations where you might have a license with a small amount of workers and another with a larger
     * amount of workers to assist in preventing acquisition of the larger worker count license.
     * @param maxWorkerCount The maximum worker count.  A value of 0 means no maximum.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withMaxWorkerCount(int maxWorkerCount) {
        this.maxWorkerCount = maxWorkerCount;
        return this;
    }

    /***
     * Allows you to provide a function which makes the final decision as to which available license to obtain.
     * Supplied function will be provided Stream of AvailableLicenses after this license resolver has applied any
     * filtering that it may have applied.
     * @param finalDecider A function that can make the final choice of which (if any) license to acquire.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withFinalDecisionMadeBy(
            @NonNull Function<Stream<AvailableLicence>, Optional<AvailableLicence>> finalDecider) {
        this.finalDecider = finalDecider;
        return this;
    }

    /***
     * Allows you to provider a license credentials callback used for license authentication (CLS/NMS).
     * @param credentialsCallback The custom credentials callback
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withLicenseCredentialsProvider(CredentialsCallback credentialsCallback) {
        this.credentialsCallback = credentialsCallback;
        return this;
    }

    /***
     * Allows you to specify the specific username and password to use for license authentication (CLS/NMS)
     * @param userName The username to use
     * @param password The password to use
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withLicenseCredentials(String userName, String password) {
        this.credentialsCallback = credentialsCallbackInfo -> {
            credentialsCallbackInfo.setUsername(userName);
            credentialsCallbackInfo.setPassword(password);
        };
        return this;
    }

    /***
     * Allows you to specify Environment variables to resolve the license authentication username and password from.
     * @param usernameEnvVar The name of the environment variable
     * @param passwordEnvVar The password of the environment variable
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withLicenseCredentialsResolvedFromEnvVars(String usernameEnvVar, String passwordEnvVar) {
        this.credentialsCallback = credentialsCallbackInfo -> {
            credentialsCallbackInfo.setUsername(System.getenv(usernameEnvVar));
            credentialsCallbackInfo.setPassword(System.getenv(passwordEnvVar));
        };
        return this;
    }

    /***
     * Specifies that license authentication credentials will be obtained from environment variables.  The value of
     * username will be pulled from "NUIX_USERNAME" and the value of password will be pulled from "NUIX_PASSWORD".
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withLicenseCredentialsResolvedFromEnvVars() {
        return withLicenseCredentialsResolvedFromEnvVars("NUIX_USERNAME", "NUIX_PASSWORD");
    }

    /***
     * Specifies a certificate trust callback.
     * @param certificateTrustCallback The certificate trust callback.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withCertificateTrustCallback(CertificateTrustCallback certificateTrustCallback) {
        this.certificateTrustCallback = certificateTrustCallback;
        return this;
    }

    /***
     * Specifies a certificate trust callback that blindly accepts all certificates.  This is the default behavior
     * of license resolver.
     * @return This license resolver for chained method calls.
     */
    public NuixLicenseResolver withTrustAllCertificates() {
        return withCertificateTrustCallback((certificateTrustCallbackInfo) -> certificateTrustCallbackInfo.setTrusted(true));
    }

    /***
     * Attempts to license the provided Engine instance using resolution and filtering configuration of this instance.
     * @param engine The engine instance to attempt to license.
     * @return True if a license was obtained, false if not.
     * @throws Exception Exceptions thrown by any of the methods working to obtain a license will be uncaught and allowed
     * to bubble up for caller to respond to.
     */
    @Override
    public boolean resolveLicense(@NonNull Engine engine) throws Exception {
        Map<String, Object> licenseOptions = Collections.emptyMap();

        log.info("License Source: "+licenseSource);

        switch (licenseSource) {
            case Cloud:
                licenseOptions = Map.of("sources", "cloud-server");
                System.setProperty("nuix.registry.servers", "https://licence-api.nuix.com");
                break;
            case Server:
                licenseOptions = Map.of("sources", "server");
                System.setProperty("nuix.registry.servers", serverHost + ":" + serverPort);
                break;
            case Dongle:
                licenseOptions = Map.of("sources", "dongle");
                break;
            case Any:
                // No further action needed
                break;
            case Custom:
                licenseOptions = Map.of("sources", customSource);
                break;
        }

        // Credentials supplier for instances which require it (server/cls)
        if (credentialsCallback != null) {
            engine.whenAskedForCredentials(credentialsCallback);
        }

        // Certificate trust callback
        if (certificateTrustCallback != null) {
            engine.whenAskedForCertificateTrust(certificateTrustCallback);
        }

        log.info("Obtaining licensor....");
        Licensor licensor = engine.getLicensor();

        log.info("Obtaining license stream...");
        Stream<AvailableLicence> availableLicensesStream = licensor.findAvailableLicencesStream(licenseOptions);

        log.info("Applying filtering to available licenses...");
        Stream<AvailableLicence> filteredLicensesStream = availableLicensesStream.filter((availableLicense -> {
            log.info("Inspecting license: "+ NuixLicenseFeaturesLogger.summarizeLicense(availableLicense));

            // It is possible to get a Licence specifically for running an NMS instance and not an Engine instance
            // which we can ignore since it cannot license an Engine instance for us.
            if (availableLicense.getShortName().equalsIgnoreCase("server")) {
                log.info("Skipping license with shortname 'server' as we cannot make use of it");
                return false;
            }

            // Get the number of workers this license has to offer
            Integer availableWorkerCount = ((LicenceProperties) availableLicense).getWorkers();

            // Verify the minimum worker count
            if (availableWorkerCount != null && minWorkerCount > 0 && availableWorkerCount < minWorkerCount) {
                log.info(String.format("License has %s workers, filter specifies a minimum of %s, ignoring this license",
                        availableWorkerCount, minWorkerCount));
                return false;
            }

            // Verify the maximum worker count.  This is intended for situations where perhaps multiple fixed worker
            // count licenses may be available, and you don't want to acquire licenses with larger worker counts.
            // When acquiring from a license server and the license shares a worker pool (canChooseWorkers below) then
            // the maximum is ignored.
            if (availableWorkerCount != null && availableLicense.canChooseWorkers() && maxWorkerCount > 0 && availableWorkerCount > maxWorkerCount) {
                log.info(String.format("License has %s workers, filter specifies a maximum of %s, ignoring this license",
                        availableWorkerCount, minWorkerCount));
                return false;
            }

            // Verify short name
            String availableLicenseShortName = availableLicense.getShortName().toLowerCase();
            if (targetShortName != null && !availableLicenseShortName.equalsIgnoreCase(targetShortName)) {
                log.info(String.format("License has shortname %s which does not match the target shortname %s",
                        availableLicenseShortName, targetShortName));
                return false;
            }

            log.info("License meets all specified criteria...");
            return true;
        }));

        // If we have a finalDecider function, allow it to pick from the remaining choices
        Optional<AvailableLicence> possiblySelectedLicense;
        if (finalDecider != null) {
            log.info("Asking provided final decider function to choose license from potential candidates...");
            possiblySelectedLicense = finalDecider.apply(filteredLicensesStream);
        } else {
            log.info("Choosing first license from potential candidates...");
            possiblySelectedLicense = filteredLicensesStream.findAny();
        }

        // If we have a license to obtain, obtain it and let caller know we have obtained it by
        // returning true.  Otherwise, return false so caller knows that no license has been resolved yet.
        if (possiblySelectedLicense.isPresent()) {
            AvailableLicence selectedLicense = possiblySelectedLicense.get();
            if (selectedLicense.canChooseWorkers()) {
                int countToAcquire = Math.max(minWorkerCount, 2);
                log.info(String.format("License supports choosing worker count, attempting to acquire with %s workers",
                        countToAcquire));
                // If we are able to select the number of workers to obtain with the license,
                // we will either use the minimum user provided or 2, whichever is higher.
                selectedLicense.acquire(Map.of("workerCount", countToAcquire));
            } else {
                log.info(String.format("License does not support choosing worker count, attempting to acquire with all %s workers",
                        ((LicenceProperties) selectedLicense).getWorkers()));
                selectedLicense.acquire();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "LicenseResolver{" +
                "licenseSource=" + licenseSource +
                ", customSource='" + customSource + '\'' +
                ", serverHost='" + serverHost + '\'' +
                ", serverPort=" + serverPort +
                ", requiredFeatures=" + requiredFeatures +
                ", minWorkerCount=" + minWorkerCount +
                ", maxWorkerCount=" + maxWorkerCount +
                ", targetShortName='" + targetShortName + '\'' +
                '}';
    }
}
