package com.example;

/**
 * A simple class that provides version information.
 * This is the Java 11 version.
 */
public class VersionInfo {

    public String getJavaVersion() {
        return "Java 11";
    }

    public String getFeatures() {
        return "Java 11 features: var keyword, HTTP Client API, String methods (isBlank, lines, strip)";
    }

    public String getRuntimeVersion() {
        return System.getProperty("java.version");
    }

    public String describe() {
        var runtime = getRuntimeVersion();
        var compiled = getJavaVersion();
        return String.format("Running on %s (compiled for %s)", runtime, compiled);
    }
}
