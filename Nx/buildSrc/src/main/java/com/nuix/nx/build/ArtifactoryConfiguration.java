package com.nuix.nx.build;

import java.util.Map;

public class ArtifactoryConfiguration {
    private final Map<String, Object> projectProperties;

    @lombok.Getter
    private final String publishArtifactRepository;
    @lombok.Getter
    private final String publishArtifactUser;
    @lombok.Getter
    private final String publishArtifactToken;
    @lombok.Getter
    private final String dependencyRepository;

    public ArtifactoryConfiguration(Map<String, Object> properties) {
        this.projectProperties = properties;

        publishArtifactRepository = projectProperties.getOrDefault("artifactRepo", "").toString();
        publishArtifactUser = projectProperties.getOrDefault("artifactUser", "").toString();
        publishArtifactToken = projectProperties.getOrDefault("artifactToken", "").toString();
        this.dependencyRepository = projectProperties.getOrDefault("nuixDependencyRepo", "").toString();
    }

}
