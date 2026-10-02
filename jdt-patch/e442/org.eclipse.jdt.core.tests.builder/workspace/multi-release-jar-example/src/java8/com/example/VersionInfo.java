package com.example;

/**
 * A simple class that provides version information.
 * This is the Java 8 compatible base version.
 */
public class VersionInfo {

    public String getJavaVersion() {
        return "Java 8";
    }

    public String getFeatures() {
        return "Base features: Lambda expressions, Streams API, Date/Time API";
    }

    public String getRuntimeVersion() {
        return System.getProperty("java.version");
    }

    public String describe() {
        return String.format("Running on %s (compiled for %s)",
            getRuntimeVersion(), getJavaVersion());
    }
}
