package com.example;

/**
 * A simple class that provides version information.
 * This is the Java 17 version using text blocks and sealed classes features.
 */
public class VersionInfo {

    public String getJavaVersion() {
        return "Java 17";
    }

    public String getFeatures() {
        return """
            Java 17 features:
            - Sealed classes
            - Pattern matching for switch (preview)
            - Text blocks
            - Records
            """;
    }

    public String getRuntimeVersion() {
        return System.getProperty("java.version");
    }

    public String describe() {
        var runtime = getRuntimeVersion();
        var compiled = getJavaVersion();
        return """
            Runtime: %s
            Compiled for: %s
            """.formatted(runtime, compiled);
    }
}
