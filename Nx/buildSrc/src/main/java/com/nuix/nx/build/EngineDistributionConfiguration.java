package com.nuix.nx.build;

import java.util.Arrays;
import java.util.Map;

public class EngineDistributionConfiguration {
    private final Map<String, Object> projectProperties;

    @lombok.Getter
    private final String nuixEngineRepo;
    private final String nuixEngineDistOs;
    private final String nuixEngineDistExtension;
    private final String nuixEngineDistMajorMinorVersion;
    private final String nuixEngineDistPointReleaseVersion;

    public EngineDistributionConfiguration(Map<String, Object> properties) {
        this.projectProperties = properties;

        nuixEngineRepo = projectProperties.getOrDefault("nuixEngineRepo", "").toString();

        String engineOs = projectProperties.getOrDefault("nuixEngineOs", "windows").toString();
        switch (engineOs) {
            case "linux":
                nuixEngineDistOs = "linux-x86_64";
                nuixEngineDistExtension = "tar.bz2";
                break;
            case "mac":
                nuixEngineDistOs = "macos-x86_64";
                nuixEngineDistExtension = "tar.gz";
                break;
            default:
                nuixEngineDistOs = "win32-amd64";
                nuixEngineDistExtension = "zip";
        }

        String engineRelease = projectProperties.getOrDefault("nuixEngineRelease", "9.10.17.1073").toString();
        String[] versionParts = engineRelease.split("\\.");
        nuixEngineDistMajorMinorVersion = versionParts[0] + "." + versionParts[1];
        nuixEngineDistPointReleaseVersion = String.join(".", Arrays.asList(versionParts).subList(2, versionParts.length));
    }

    public String getEngineDistributionName() {
        return String.format("engine-dist-%s-%s.%s.%s",
                nuixEngineDistOs,
                nuixEngineDistMajorMinorVersion,
                nuixEngineDistPointReleaseVersion,
                nuixEngineDistExtension);
    }

    public String getEngineDistributionPath() {
        return String.format("%s/%s/%s",
                nuixEngineRepo,
                nuixEngineDistMajorMinorVersion,
                getEngineDistributionName());
    }

}
