# Multi-Release JAR Example

Sources of the MR jar used by 
- org.eclipse.jdt.core.tests.builder.BuildpathTests.testIncompatibleJdkLevelOnMrJar

This is a simple example of a multi-release JAR file

## Structure

- **Java 8 base version** in the root `com/example/` directory
- **Java N version** in `META-INF/versions/N/com/example/` directories for Java 11, 17, 21, and 25

## Building

### Automated Build

Use the provided script. This will automatically compile all Java versions (8, 11, 17, 21, 25) and create the multi-release JAR.

### Windows:

Use the Batch script `build.bat`

### *nix

Use the Bash script `build.sh`

### Manual Build

The JAR can be built manually with:

```
javac --release 8 -d bin8 src/java8/com/example/*.java
javac --release 9 -d bin9 src/java9/*.java
javac --release 11 -d bin11 src/java11/com/example/*.java
javac --release 17 -d bin17 src/java17/com/example/*.java
javac --release 21 -d bin21 src/java21/com/example/*.java
javac --release 25 -d bin25 src/java25/com/example/*.java
jar cfm multi-release-example.jar META-INF/MANIFEST.MF bin*
```

## Testing

```
java -cp multi-release-example.jar com.example.TestMultiRelease
```

The JVM will automatically select the appropriate class version

## Files

- `multi-release-example.jar` - The multi-release JAR (copy to parent directory to use for the unit test)
- `src/java8/` - Java 8 base source (includes TestMultiRelease.java)
- `src/java9/` - Java 9 Module Info (ending up in root of MR JAR)
- `src/java11/` - Java 11 version-specific source
- `src/java17/` - Java 17 version-specific source
- `src/java21/` - Java 21 version-specific source
- `src/java25/` - Java 25 version-specific source
- `META-INF/MANIFEST.MF` - Manifest with Multi-Release: true
- `build.bat` - Build script (Batch for Windows)
- `build.sh` - Build script (Bash for *nix)
