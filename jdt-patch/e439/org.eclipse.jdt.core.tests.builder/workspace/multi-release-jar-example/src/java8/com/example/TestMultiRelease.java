package com.example;
public class TestMultiRelease {
    public static void main(String[] args) {
        VersionInfo info = new VersionInfo();
        System.out.println("=== Multi-Release JAR Test ===");
        System.out.println("Compiled for: " + info.getJavaVersion());
        System.out.println("Runtime: " + info.getRuntimeVersion());
        System.out.println();
        System.out.println("Features:");
        System.out.println(info.getFeatures());
        System.out.println();
        System.out.println("Description:");
        System.out.println(info.describe());
    }
}
