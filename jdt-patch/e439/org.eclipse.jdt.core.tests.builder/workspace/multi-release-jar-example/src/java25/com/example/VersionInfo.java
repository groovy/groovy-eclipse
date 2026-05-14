package com.example;

/**
 * A simple class that provides version information.
 * This is the Java 25 version with latest language features.
 */
public class VersionInfo {

    public String getJavaVersion() {
        return "Java 25";
    }

    public String getFeatures() {
        return """
            Java 25 features:
            - Flexible constructor bodies (JEP 492)
            - Primitive types in patterns and instanceof (JEP 455)
            - Module import declarations (JEP 476)
            - Stream gatherers (JEP 485)
            - Class-file API (finalized)
            """;
    }

    public String getRuntimeVersion() {
        return System.getProperty("java.version");
    }

    public String describe() {
        var runtime = getRuntimeVersion();
        var compiled = getJavaVersion();
        // Using enhanced pattern matching
        var javaType = switch (compiled) {
            case String s when s.contains("25") -> "Latest Version";
            case String s when s.contains("21") -> "Previous LTS";
            case String s when s.contains("17") -> "Older LTS";
            default -> "Legacy Version";
        };

        var featureCount = getFeatures().lines().filter(l -> l.contains("-")).count();

        return """
            ┌─────────────────────────────────┐
            │ Java Version Information        │
            ├─────────────────────────────────┤
            │ Runtime: %-23s│
            │ Compiled for: %-18s│
            │ Type: %-26s│
            │ Features shown: %-16d│
            └─────────────────────────────────┘
            """.formatted(runtime, compiled, javaType, featureCount);
    }
}
