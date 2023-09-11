package com.nuix.nx.build;

import java.nio.file.Paths;
import java.util.Map;

public class TestingConfiguration {
    private final Map<String, Object> projectProperties;

    @lombok.Getter
    private final String tempDirectory;
    @lombok.Getter
    private final String testDataDirectory;
    @lombok.Getter
    private final String rubyExamplesDirectory;
    @lombok.Getter
    private final String testOutputDirectoryRoot;
    @lombok.Getter
    private final String nuixUsername;
    @lombok.Getter
    private final String nuixPassword;

    public TestingConfiguration(Map<String, Object> properties) {
        this.projectProperties = properties;

        tempDirectory = projectProperties.getOrDefault("tempDirectory",
                Paths.get(System.getenv("LOCALAPPDATA"), "Temp", "Nuix").toAbsolutePath()).toString();
        testDataDirectory = projectProperties.getOrDefault("testDataDir",
                Paths.get(projectProperties.get("projectDir").toString(),
                        "..", "TestData")
                        .normalize()
                        .toAbsolutePath())
                .toString();
        rubyExamplesDirectory = projectProperties.getOrDefault("rubyExamplesDirectory",
                        Paths.get(projectProperties.get("projectDir").toString(),
                                        "..", "Examples")
                                .normalize()
                                .toAbsolutePath())
                .toString();

        testOutputDirectoryRoot = projectProperties.getOrDefault("testOutputDirectoryRoot",
                        Paths.get(projectProperties.get("projectDir").toString(),
                                        "..", "TestOutput")
                                .normalize()
                                .toAbsolutePath())
                .toString();

        nuixUsername = projectProperties.getOrDefault("nuixUsername", System.getenv("NUIX_USERNAME")).toString();
        nuixPassword = projectProperties.getOrDefault("nuixPassword", System.getenv("NUIX_PASSWORD")).toString();
    }
}