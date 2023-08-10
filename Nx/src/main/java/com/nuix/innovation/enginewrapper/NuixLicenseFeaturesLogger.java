package com.nuix.innovation.enginewrapper;

import nuix.Licence;
import nuix.LicenceProperties;
import nuix.engine.AvailableLicence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringJoiner;

/***
 * Helper class for logging what features are present on a given license.
 * @author Jason Wells
 */
public class NuixLicenseFeaturesLogger {
    // Obtain a logger instance for this class
    private final static Logger logger = LogManager.getLogger("LicenseFeatures");

    // List of license features copied from 9.10 license profiles documentation
    private static final String[] knownFeatures = new String[]{
            "ANALYSIS", "AOS_DATA", "AUTOMATIC_CLASSIFIER_EDITING", "AXS_ONE", "CASE_CREATION", "CUSTOM_NAMED_ENTITIES",
            "CYBER_CONTEXT", "DESKTOP", "ELASTIC_SEARCH", "EXCHANGE_WS", "EXPORT_CASE_SUBSET", "EXPORT_DISCOVER",
            "EXPORT_ITEMS", "EXPORT_LEGAL", "EXPORT_SINGLE_ITEM", "EXPORT_VIEW", "FAST_REVIEW", "FRONT_LOAD_METADATA",
            "GENERAL_DATA", "GRAPH", "GWAVA", "IMAP_POP", "LIGHT_SPEED", "LOG_STASH", "LOTUS_NOTES", "MAIL_XTENDER",
            "METADATA_IMPORT", "MICROSOFT_GRAPH", "MOBILE_DEVICE_IMAGING", "NETWORK_DATA", "OCR_PROCESSING",
            "OTHER_EMAIL", "OUTLOOK", "OUTLOOK_EXPRESS", "PARTIAL_LOAD", "PRODUCTION_SET", "SCRIPTING",
            "SYMANTEC_VAULT", "UNRESTRICTED_CASE_ACCESS", "WORKER", "WORKER_SCRIPTING", "ZANTAZ"
    };

    /***
     * Returns a String array containing a list of known license features
     * @return Array of known license features
     */
    public static String[] getKnownFeatures() {
        return knownFeatures;
    }

    /***
     * Generates a String summarizing the details of the provided AvailableLicence.
     * @param availableLicense The AvailableLicense to summarize
     * @return A String summarizing the AvailableLicense
     */
    public static String summarizeLicense(AvailableLicence availableLicense) {
        if(availableLicense == null) { return "NULL LICENSE"; }
        String result = String.format("[Location=%s, Type=%s, ShortName=%s, Description=%s, Count=%s, Workers=%s, Features=%s]",
                availableLicense.getSource().getLocation(),
                availableLicense.getSource().getType(),
                availableLicense.getShortName(),
                availableLicense.getDescription(),
                availableLicense.getCount(),
                ((LicenceProperties) availableLicense).getWorkers(),
                String.join("; ", availableLicense.getAllEnabledFeatures())
        );
        return result;
    }

    /***
     * Generates a String summarizing the details of the provided License.
     * @param license The Licence to summarize
     * @return A String summarizing the Licence
     */
    public static String summarizeLicense(Licence license) {
        if(license == null) { return "NULL LICENSE"; }
        String result = String.format("[ShortName=%s, Description=%s, Workers=%s, Features=%s]",
                license.getShortName(),
                license.getDescription(),
                ((LicenceProperties) license).getWorkers(),
                String.join("; ", license.getAllEnabledFeatures())
        );
        return result;
    }

    /***
     * Logs a listing of whether each feature is present or not on the provided license.
     * @param license The license to log feature presence information about
     */
    public static void logFeaturesOfLicense(LicenceProperties license) {
        StringJoiner message = new StringJoiner("\n");
        message.add("License Features:");
        for (String feature : knownFeatures) {
            boolean hasFeature = license.hasFeature(feature);
            message.add(String.format("[%s] %s", hasFeature ? "X" : " ", feature));
        }
        logger.info(message.toString());
    }
}
