package com.nuix.nx.build;

import java.util.Map;

public class ProjectConfiguration {
    private final Map<String, Object> projectProperties;
    @lombok.Getter
    private final String groupName;
    @lombok.Getter
    private final String versionString;
    @lombok.Getter
    private final int targetJreVersion;

    public ProjectConfiguration(Map<String, Object> properties) {
        this.projectProperties = properties;

        targetJreVersion = ((Number)projectProperties.getOrDefault("targetJreVersion", 11)).intValue();

        String groupText = projectProperties.getOrDefault("group", "com.nuix.nx").toString();
        if(null == groupText || groupText.isEmpty()) {
            groupName = "com.nuix.nx";
        } else {
            groupName = groupText;
        }

        String versionText = projectProperties.getOrDefault("version", "1.20.0-SNAPSHOT").toString();
        if(null == versionText || versionText.isEmpty() || "unspecified".equals(versionText)) {
            versionString = "1.20.0-SNAPSHOT";
        } else {
            versionString = versionText;
        }
    }

}