package com.nuix.nx.build;

import java.nio.file.Paths;
import java.util.Map;

public class EnvironmentConfiguration {
    private final Map<String, Object> projectProperties;

    @lombok.Getter
    private String nuixEngineDirectory;
    @lombok.Getter
    private String nuixEngineLib;
    @lombok.Getter
    private final boolean useRepository;

    @lombok.Getter
    private ProjectConfiguration baseConfigs;
    @lombok.Getter
    private ArtifactoryConfiguration artifactory;
    @lombok.Getter
    private EngineDistributionConfiguration engineDistro;
    @lombok.Getter
    private TestingConfiguration testing;

    public EnvironmentConfiguration(Map<String, Object> properties) {
        this.projectProperties = properties;

        Object engineDir = projectProperties.getOrDefault("nuixEngineDir", System.getenv("NUIX_ENGINE_DIR"));

        useRepository = (null == engineDir || engineDir.toString().isEmpty());
        if (!useRepository) {
            setNuixEngineDirectory(engineDir.toString());
        }

        baseConfigs = new ProjectConfiguration(properties);
        artifactory = new ArtifactoryConfiguration(properties);
        engineDistro = new EngineDistributionConfiguration(properties);
        testing = new TestingConfiguration(properties);
    }

    public void setNuixEngineDirectory(String engineDir) {
        nuixEngineDirectory = engineDir;
        nuixEngineLib = Paths.get(nuixEngineDirectory, "lib").toAbsolutePath().toString();
    }

    public String getNuixBinDirectory() {
        return Paths.get(nuixEngineDirectory, "bin").toAbsolutePath().toString();
    }

    public String getNuixBinX86Directory() {
        return Paths.get(nuixEngineDirectory, "bin", "x86").toAbsolutePath().toString();
    }
}