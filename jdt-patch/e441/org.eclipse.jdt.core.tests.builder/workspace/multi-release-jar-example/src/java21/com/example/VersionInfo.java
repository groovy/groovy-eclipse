package com.example;

/**
 * A simple class that provides version information.
 * This is the Java 21 version using pattern matching for switch and record patterns.
 */
public class VersionInfo {

    public String getJavaVersion() {
        return "Java 21";
    }

    public String getFeatures() {
        return """
            Java 21 features:
            - Pattern matching for switch (finalized)
            - Record patterns
            - Virtual threads (Project Loom)
            - Sequenced collections
            - String templates (preview)
            """;
    }

    public String getRuntimeVersion() {
        return System.getProperty("java.version");
    }

    public String describe() {
        var runtime = getRuntimeVersion();
        var compiled = getJavaVersion();
        // Using pattern matching for switch with String
        var javaType = switch (compiled) {
            case String s when s.contains("21") -> "LTS Version";
            case String s when s.contains("17") -> "Previous LTS";
            default -> "Standard Version";
        };
        return """
            Runtime: %s
            Compiled for: %s (%s)
            """.formatted(runtime, compiled, javaType);
    }
}
